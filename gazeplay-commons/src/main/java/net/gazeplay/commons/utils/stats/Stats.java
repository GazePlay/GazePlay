package net.gazeplay.commons.utils.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.FixationSequence;
import net.gazeplay.commons.utils.HeatMap;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.VideoFormatKeys;
import org.monte.media.gui.Worker;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static java.lang.Math.pow;


/**
 * Created by schwab on 16/08/2017.
 */
@Slf4j
@ToString
public class Stats implements GazeMotionListener {

    private static final int trail = 10;
    private static final int fixationTrail = 50;
    private final double heatMapPixelSize;
    public final Scene gameContextScene;
    protected String gameName;

    long startTime;
    int sceneCounter = 0;
    private EventHandler<MouseEvent> recordMouseMovements;
    private EventHandler<GazeEvent> recordGazeMovements;
    private LifeCycle lifeCycle = new LifeCycle();
    private RoundsDurationReport roundsDurationReport = new RoundsDurationReport();
    private LevelsReport levelsReport = new LevelsReport();
    private ChiReport chiReport = new ChiReport();
    private int counter = 0;
    private final List<CoordinatesTracker> movementHistory = new ArrayList<>();

    private long previousTime = 0;

    private int previousXMouse = 0;
    private int previousYMouse = 0;
    private int previousXGaze = 0;
    private int previousYGaze = 0;

    private File movieFolder;
    private final boolean convexHULL = true;
    private ScreenRecorder screenRecorder;
    private ArrayList<TargetAOI> targetAOIList = null;
    private double[][] heatMap;

    private long currentTimeMillisScreenshot = System.currentTimeMillis();

    @Getter
    public int nbGoalsReached = 0;

    @Getter
    protected int nbGoalsToReach = 0;

    @Setter
    private long accidentalShotPreventionPeriod = 0;

    @Getter
    private int nbUnCountedGoalsReached;

    @Getter
    @Setter
    private long currentGazeTime;

    @Getter
    @Setter
    private long lastGazeTime;

    @Getter
    private ArrayList<LinkedList<FixationPoint>> fixationSequence;

    @Getter
    private SavedStatsInfo savedStatsInfo;

    @Getter
    private WritableImage gameScreenShot;

    JsonArray coordinateData = new JsonArray();
    private final JsonObject savedDataObj = new JsonObject();
    String currentGameVariant;
    String currentGameNameCode;
    double currentGameSeed = 0.;
    private final boolean inReplayMode;

    private String directoryOfVideo;

    private String nameOfVideo;

    private Long currentRoundStartTime;

    public String variantType = "";

    //Phonology
    public int totalPhonology = 0;
    public int simpleScoreItemsPhonology = 0;
    public int complexScoreItemsPhonology = 0;
    public int scoreLeftTargetItemsPhonology = 0;
    public int scoreRightTargetItemsPhonology = 0;

    //Semantics
    public int totalSemantic = 0;
    public int simpleScoreItemsSemantic = 0;
    public int complexScoreItemsSemantic = 0;
    public int frequentScoreItemSemantic = 0;
    public int infrequentScoreItemSemantic = 0;
    public int scoreLeftTargetItemsSemantic = 0;
    public int scoreRightTargetItemsSemantic = 0;

    //Morphosyntax
    public int totalMorphosyntax = 0;
    public int simpleScoreItemsMorphosyntax = 0;
    public int complexScoreItemsMorphosyntax = 0;
    public int scoreLeftTargetItemsMorphosyntax = 0;
    public int scoreRightTargetItemsMorphosyntax = 0;

    //Word comprehension
    public int totalWordComprehension = 0;
    public int totalItemsAddedManually = 0;

    public int total = 0;
    public long timeGame = 0;
    public String actualFile = "";

    //Gazeplay Eval
    public String[] nameScores;
    public int[] scores;
    public int[] maxScores;

    //parameters for AOI
    private int movementHistoryidx = 0;
    private final List<AreaOfInterestProps> allAOIList = new ArrayList<>();
    private List<CoordinatesTracker> areaOfInterestList = new ArrayList<>();
    @Getter
    private final List<List> allAOIListTemp = new ArrayList<>();
    @Getter
    private final List<int[]> startAndEndIdx = new ArrayList<>();
    @Getter
    private final List<Polygon> allAOIListPolygon = new ArrayList<>();
    @Getter
    private final List<Double[]> allAOIListPolygonPt = new ArrayList<>();
    private final double highestFixationTime = 0;
    private final Configuration config = ActiveConfigurationContext.getInstance();
    private int colorIterator;
    private final javafx.scene.paint.Color[] colors = new javafx.scene.paint.Color[]{
        javafx.scene.paint.Color.PURPLE,
        javafx.scene.paint.Color.WHITE,
        javafx.scene.paint.Color.PINK,
        javafx.scene.paint.Color.ORANGE,
        javafx.scene.paint.Color.BLUE,
        javafx.scene.paint.Color.RED,
        javafx.scene.paint.Color.CHOCOLATE
    };

    private static boolean configMenuOpen = false;

    public Stats(final Scene gameContextScene) {
        this(gameContextScene, null);
    }

    public Stats(final Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        this(gameContextScene, null, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    public Stats(final Scene gameContextScene, final String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;
        this.inReplayMode = false;

        heatMapPixelSize = computeHeatMapPixelSize(gameContextScene);
    }

    public Stats(final Scene gameContextScene, final String gameName, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;
        this.nbGoalsReached = nbGoalsReached;
        this.nbGoalsToReach = nbGoalsToReach;
        this.nbUnCountedGoalsReached = nbUnCountedGoalsReached;
        this.fixationSequence = fixationSequence;
        this.lifeCycle = lifeCycle;
        this.roundsDurationReport = roundsDurationReport;
        this.savedStatsInfo = savedStatsInfo;
        this.inReplayMode = true;

        heatMapPixelSize = computeHeatMapPixelSize(gameContextScene);
    }

    static double[][] instantiateHeatMapData(final Scene gameContextScene, final double heatMapPixelSize) {
        final int heatMapWidth = (int) (gameContextScene.getHeight() / heatMapPixelSize);
        final int heatMapHeight = (int) (gameContextScene.getWidth() / heatMapPixelSize);
        log.info("heatMapWidth = {}, heatMapHeight = {}", heatMapWidth, heatMapHeight);
        return new double[heatMapWidth][heatMapHeight];
    }

    public ArrayList<TargetAOI> getTargetAOIList() {
        return this.targetAOIList;
    }

    public void setTargetAOIList(final ArrayList<TargetAOI> targetAOIList) {
        this.targetAOIList = targetAOIList;
        for (int i = 0; i < targetAOIList.size() - 1; i++) {
            final long duration = targetAOIList.get(i).getTimeEnded() - targetAOIList.get(i).getTimeStarted();
            this.targetAOIList.get(i).setDuration(duration);
        }
        if (targetAOIList.size() >= 1) {
            targetAOIList.get(targetAOIList.size() - 1).setDuration(0);
        }
    }

    public void notifyNewRoundReady() {
        currentRoundStartTime = System.currentTimeMillis();
        takeScreenShot();
    }

    public void notifyNextRound() {
        final long currentRoundEndTime = System.currentTimeMillis();
        final long currentRoundDuration = currentRoundEndTime - this.currentRoundStartTime;
        this.roundsDurationReport.addRoundDuration(currentRoundDuration);
        currentRoundStartTime = currentRoundEndTime;
    }

    public void startVideoRecording() {
        directoryOfVideo = getGameStatsOfTheDayDirectory().toString();
        this.movieFolder = new File(directoryOfVideo);
        final float quality = 1.0F;
        final byte bitDepth = 24;

        final String mimeType;
        final String videoFormatName;
        final String compressorName;

        mimeType = "video/avi";
        videoFormatName = "tscc";
        compressorName = "Techsmith Screen Capture";

        System.setProperty("java.awt.headless", "false");
        final GraphicsConfiguration cfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();
        final Rectangle areaRect;
        final Dimension outputDimension;
        areaRect = cfg.getBounds();

        outputDimension = areaRect.getSize();
        final byte screenRate;
        screenRate = 30;

        Format fileFormat = new Format(
            VideoFormatKeys.MediaTypeKey,
            FormatKeys.MediaType.FILE,
            VideoFormatKeys.MimeTypeKey,
            mimeType
        );

        Format screenFormat = new Format(
            VideoFormatKeys.MediaTypeKey,
            FormatKeys.MediaType.VIDEO,
            VideoFormatKeys.EncodingKey,
            videoFormatName,
            VideoFormatKeys.CompressorNameKey,
            compressorName,
            VideoFormatKeys.WidthKey,
            outputDimension.width,
            VideoFormatKeys.HeightKey,
            outputDimension.height,
            VideoFormatKeys.DepthKey,
            (int) bitDepth,
            VideoFormatKeys.FrameRateKey,
            Rational.valueOf(screenRate),
            VideoFormatKeys.QualityKey,
            quality,
            VideoFormatKeys.KeyFrameIntervalKey,
            screenRate * 60
        );

        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");
            nameOfVideo = this.movieFolder + "/ScreenRecording " + dateFormat.format(new Date());
            this.screenRecorder = new ScreenRecorder(cfg, areaRect, fileFormat, screenFormat, null, null, this.movieFolder);
            this.screenRecorder.start();
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
        this.screenRecorder.setAudioMixer(null);
    }

    public void endVideoRecording() {
        final ScreenRecorder recorder = this.screenRecorder;
        (new Worker<>() {
            @Override
            protected Object construct() throws Exception {
                recorder.stop();
                return null;
            }

            @Override
            protected void finished() {
            }
        }).start();
    }

    private void generateAOIList(final int index) {
        final double x1 = movementHistory.get(index).getXValue();
        final double y1 = movementHistory.get(index).getYValue();
        final double x2 = movementHistory.get(index - 1).getXValue();
        final double y2 = movementHistory.get(index - 1).getYValue();
        final double eDistance = Math.sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));
        if (eDistance < 150 && movementHistory.get(index).getIntervalTime() > 10) {
            if (index == 1) {
                areaOfInterestList.add(movementHistory.get(0));
            }
            areaOfInterestList.add(movementHistory.get(index));
        } else {
            if (areaOfInterestList.size() > 2) {

                allAOIListTemp.add(new ArrayList<>(areaOfInterestList));
                int[] startEnd = new int[]{index - areaOfInterestList.size(), index};
                startAndEndIdx.add(startEnd);

                final Point2D[] points = new Point2D[areaOfInterestList.size()];

                for (int i = 0; i < areaOfInterestList.size(); i++) {
                    CoordinatesTracker coordinate = areaOfInterestList.get(i);
                    points[i] = new Point2D(coordinate.getXValue(), coordinate.getYValue());
                }

                final Double[] polygonPoints;

                // Uncomment to use convex hull
                // if (config.isConvexHullDisabled()) {
                // polygonPoints = calculateConvexHull(points);
                // } else {
                polygonPoints = calculateRectangle(points);
                // }

                final Polygon areaOfInterest = new Polygon();
                areaOfInterest.getPoints().addAll(polygonPoints);
                allAOIListPolygonPt.add(polygonPoints);

                colorIterator = index % 7;
                areaOfInterest.setStroke(colors[colorIterator]);
                allAOIListPolygon.add(areaOfInterest);
            } else if (eDistance > 700) {
                areaOfInterestList.add(movementHistory.get(index));
                allAOIListTemp.add(new ArrayList<>(areaOfInterestList));
                int[] startEnd = new int[]{index - areaOfInterestList.size(), index};
                startAndEndIdx.add(startEnd);
                final float radius = 15;
                final Point2D[] points = new Point2D[8];

                for (int i = 0; i < 8; i++) {
                    CoordinatesTracker coordinate = areaOfInterestList.get(0);
                    points[i] = new Point2D(coordinate.getXValue() + pow(-1, i) * radius, coordinate.getYValue() + pow(-1, i) * radius);
                }

                final Double[] polygonPoints;

                // Uncomment to use convex hull
                // if (config.isConvexHullDisabled()) {
                //     polygonPoints = calculateConvexHull(points);
                // } else {
                polygonPoints = calculateRectangle(points);
                // }

                final Polygon areaOfInterest = new Polygon();
                areaOfInterest.getPoints().addAll(polygonPoints);
                allAOIListPolygonPt.add(polygonPoints);

                colorIterator = index % 7;
                areaOfInterest.setStroke(colors[colorIterator]);
                allAOIListPolygon.add(areaOfInterest);
            }
            areaOfInterestList = new ArrayList<>();
        }
    }

    static Double[] calculateRectangle(final Point2D[] point2D) {
        double leftPoint = point2D[0].getX();
        double rightPoint = point2D[0].getX();
        double topPoint = point2D[0].getY();
        double bottomPoint = point2D[0].getY();

        for (int i = 1; i < point2D.length; i++) {
            if (point2D[i].getX() < leftPoint) {
                leftPoint = point2D[i].getX();
            }
            if (point2D[i].getX() > rightPoint) {
                rightPoint = point2D[i].getX();
            }
            if (point2D[i].getY() > topPoint) {
                topPoint = point2D[i].getY();
            }
            if (point2D[i].getY() < bottomPoint) {
                bottomPoint = point2D[i].getY();
            }
        }

        final Double[] squarePoints = new Double[8];
        final int bias = 15;

        squarePoints[0] = leftPoint - bias;
        squarePoints[1] = topPoint + bias;
        squarePoints[2] = rightPoint + bias;
        squarePoints[3] = topPoint + bias;
        squarePoints[4] = rightPoint + bias;
        squarePoints[5] = bottomPoint - bias;
        squarePoints[6] = leftPoint - bias;
        squarePoints[7] = bottomPoint - bias;
        return squarePoints;
    }

    static Double[] calculateConvexHull(final Point2D[] points) {
        final int numberOfPoints = points.length;
        final ArrayList<Double> convexHullPoints = new ArrayList<>();
        final Vector<Point2D> hull = new Vector<>();

        // Finding the index of the lowest X value, or left-most point, in all points.
        int lowestValueIndex = 0;
        for (int i = 1; i < numberOfPoints; i++) {
            if (points[i].getX() < points[lowestValueIndex].getX()) {
                lowestValueIndex = i;
            }
        }

        int point = lowestValueIndex, q;
        do {
            hull.add(points[point]);
            q = (point + 1) % numberOfPoints;
            for (int i = 0; i < numberOfPoints; i++) {
                if (orientation(points[point], points[i], points[q]) < 0) { // Checking if the points are convex.
                    q = i;
                }
            }
            point = q;
        } while (point != lowestValueIndex);

        for (final Point2D temp : hull) {
            convexHullPoints.add(temp.getX());
            convexHullPoints.add(temp.getY());
        }

        Double[] hullPointsArray = new Double[convexHullPoints.size()];
        convexHullPoints.toArray(hullPointsArray);

        return hullPointsArray;
    }

    static int orientation(final Point2D p1, final Point2D p2, final Point2D p3) {

        final int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
            - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        if (val == 0) {
            return 0;
        }

        return (val > 0) ? 1 : -1;
    }

    public void start() {

        final Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isVideoRecordingEnabled()) {
            startVideoRecording();
        }
        lifeCycle.start(() -> {
            if (!config.isHeatMapDisabled()) {
                heatMap = instantiateHeatMapData(gameContextScene, heatMapPixelSize);
            }
            if (!config.isFixationSequenceDisabled()) {
                fixationSequence = new ArrayList<LinkedList<FixationPoint>>(List.of(new LinkedList<FixationPoint>(), new LinkedList<FixationPoint>()));
            }
            startTime = System.currentTimeMillis();

            recordGazeMovements = e -> {
                if (e.getSource() == gameContextScene.getRoot() && e.getTarget() == gameContextScene.getRoot()) {
                    final long timeToFixation = System.currentTimeMillis() - startTime;
                    final long timeInterval = (timeToFixation - previousTime);
                    Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                    final int getX = (int) toSceneCoordinate.getX();
                    final int getY = (int) toSceneCoordinate.getY();
                    if (getX > 0 && getY > 0) {

                        setJSONCoordinates(timeToFixation, getX, getY, "gaze");

                        if (!config.isHeatMapDisabled()) {
                            incrementHeatMap(getX, getY);
                        }
                        if (!config.isFixationSequenceDisabled()) {
                            incrementFixationSequence(getX, getY, fixationSequence.get(FixationSequence.GAZE_FIXATION_SEQUENCE));
                        }

                        if (config.isAreaOfInterestDisabled()) {
                            if (getX != previousXMouse || getY != previousYMouse) {
                                previousXMouse = getX;
                                previousYMouse = getY;
                                movementHistory
                                    .add(new CoordinatesTracker(getX, getY, timeInterval, System.currentTimeMillis()));
                                movementHistoryidx++;
                                if (movementHistoryidx > 1) {
                                    generateAOIList(movementHistoryidx - 1);
                                }
                                previousTime = timeToFixation;
                            }
                        }
                    }
                }
                if (config.isMultipleScreenshotsEnabled() && e.getSource() == gameContextScene.getRoot()){
                    takeScreenshotWithThread();
                }
            };

            recordMouseMovements = e -> {
                if (!configMenuOpen) {
                    final long timeElapsedMillis = System.currentTimeMillis() - startTime;
                    final long timeInterval = (timeElapsedMillis - previousTime);
                    Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                    final int getX = (int) toSceneCoordinate.getX();
                    final int getY = (int) toSceneCoordinate.getY();
                    if (getX > 0 || getY > 0) {

                        setJSONCoordinates(timeElapsedMillis, getX, getY, "mouse");

                        if (!config.isHeatMapDisabled()) {
                            incrementHeatMap(getX, getY);
                        }
                        if (!config.isFixationSequenceDisabled()) {
                            incrementFixationSequence(getX, getY, fixationSequence.get(FixationSequence.MOUSE_FIXATION_SEQUENCE));
                        }

                        if (config.isAreaOfInterestDisabled()) {
                            if (getX != previousXGaze || getY != previousYGaze && counter == 2) {
                                previousXGaze = getX;
                                previousYGaze = getY;
                                movementHistory
                                    .add(new CoordinatesTracker(getX, getY, timeInterval, System.currentTimeMillis()));
                                movementHistoryidx++;
                                if (movementHistoryidx > 1) {
                                    generateAOIList(movementHistoryidx - 1);
                                }
                                previousTime = timeElapsedMillis;
                                counter = 0;
                            }
                            counter++;
                        }
                    }
                }
                if (config.isMultipleScreenshotsEnabled() && e.getSource() == gameContextScene.getRoot()){
                    takeScreenshotWithThread();
                }
            };

            gameContextScene.getRoot().addEventFilter(GazeEvent.ANY, recordGazeMovements);
            gameContextScene.getRoot().addEventFilter(MouseEvent.ANY, recordMouseMovements);

        });
        currentRoundStartTime = lifeCycle.getStartTime();
    }

    private void setJSONCoordinates(long timeElapsedMillis, int getX, int getY, String event) {
        JsonObject coordinates = new JsonObject();
        coordinates.addProperty("X", getX / gameContextScene.getWidth());
        coordinates.addProperty("Y", getY / gameContextScene.getHeight());
        coordinates.addProperty("time", timeElapsedMillis);
        coordinates.addProperty("event", event);
        saveCoordinates(coordinates);
    }

    public List<CoordinatesTracker> getMovementHistoryWithTime() {
        return this.movementHistory;
    }

    public void reset() {
        nbGoalsReached = 0;
        nbGoalsToReach = 0;
        accidentalShotPreventionPeriod = 0;

        roundsDurationReport = new RoundsDurationReport();
        lifeCycle = new LifeCycle();
        start();
    }

    public void stop() {
        final Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isVideoRecordingEnabled()) {
            endVideoRecording();
        }
        lifeCycle.stop(() -> {
            if (recordGazeMovements != null) {
                gameContextScene.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
            }
            if (recordMouseMovements != null) {
                gameContextScene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
            }
        });
    }

    // function used for testing purposes
    @Override
    public void gazeMoved(final javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incrementHeatMap(positionX, positionY);
        incrementFixationSequence(positionX, positionY, fixationSequence.get(FixationSequence.GAZE_FIXATION_SEQUENCE));
    }

    static void saveImageAsPng(final BufferedImage bufferedImage, final File outputFile) {
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Graphics initGazeMetricsImage(BufferedImage bImage, BufferedImage screenshotImage) {

        final Graphics graphics = bImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
        graphics.drawImage(screenshotImage, 0, 0, null);

        return graphics;
    }

    private BufferedImage newBufferImage(BufferedImage screenshotImage) {
        return new BufferedImage(
            screenshotImage.getWidth() + (heatMap != null ? screenshotImage.getWidth() / 20 + 10 : 0),
            screenshotImage.getHeight(), screenshotImage.getType());
    }


    private void addHeatMapToMetrics(HeatMap hm, BufferedImage bImage, Graphics graphics, BufferedImage screenshotImage) {
        BufferedImage heatmapImage = SwingFXUtils.fromFXImage(hm.getImage(), null);
        final Kernel kernel = new Kernel(3, 3,
            new float[]{1 / 16f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 4f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 16f});
        final BufferedImageOp op = new ConvolveOp(kernel);
        heatmapImage = op.filter(heatmapImage, null);

        final BufferedImage keyMouse = SwingFXUtils.fromFXImage(hm.getColorKey(bImage.getWidth() / 20, bImage.getHeight() / 2),
            null);

        graphics.drawImage(heatmapImage, 0, 0, screenshotImage.getWidth(), screenshotImage.getHeight(), null);
        graphics.drawImage(keyMouse, bImage.getWidth() - keyMouse.getWidth(), (bImage.getHeight() - keyMouse.getHeight()) / 2, null);
    }

    private void addFixationSequence(int fixationSequenceIndex, Graphics gMouseOrGaze, Graphics gMouseAndGaze, BufferedImage screenshotImage) {
        if (this.fixationSequence.get(fixationSequenceIndex) != null && fixationSequence.get(fixationSequenceIndex).size() > 0) {
            final FixationSequence scanpath = new FixationSequence((int) gameContextScene.getWidth(),
                (int) gameContextScene.getHeight(), fixationSequence, fixationSequenceIndex);
            fixationSequence.set(fixationSequenceIndex, scanpath.getSequence());
            final BufferedImage seqImage = SwingFXUtils.fromFXImage(scanpath.getImage(), null);
            gMouseOrGaze.drawImage(seqImage, 0, 0, screenshotImage.getWidth(), screenshotImage.getHeight(), null);
            gMouseAndGaze.drawImage(seqImage, 0, 0, screenshotImage.getWidth(), screenshotImage.getHeight(), null);
        }
    }

    public SavedStatsInfo saveStats() throws IOException {
        final Configuration config = ActiveConfigurationContext.getInstance();

        final File todayDirectory = getGameStatsOfTheDayDirectory();
        final String now = DateUtils.dateTimeNow();
        final String heatmapFilePrefix = now + "-heatmap";
        final String gazeMetricsFilePrefixMouse = now + "-metricsMouse";
        final String gazeMetricsFilePrefixGaze = now + "-metricsGaze";
        final String gazeMetricsFilePrefixMouseAndGaze = now + "-metricsMouseAndGaze";
        final String screenShotFilePrefix = now + "-screenshot";
        final String colorBandsFilePrefix = now + "-colorBands";
        final String replayDataFilePrefix = now + "-replayData";

        final File gazeMetricsFileMouse = new File(todayDirectory, gazeMetricsFilePrefixMouse + ".png");
        final File gazeMetricsFileGaze = new File(todayDirectory, gazeMetricsFilePrefixGaze + ".png");
        final File gazeMetricsFileMouseAndGaze = new File(todayDirectory, gazeMetricsFilePrefixMouseAndGaze + ".png");
        final File heatMapCsvFile = new File(todayDirectory, heatmapFilePrefix + ".csv");
        final File screenShotFile = new File(todayDirectory, screenShotFilePrefix + ".png");
        final File colorBandsFile = new File(todayDirectory, colorBandsFilePrefix + ".png");
        final File replayDataFile = new File(todayDirectory, replayDataFilePrefix + ".json");

        final BufferedImage screenshotImage = SwingFXUtils.fromFXImage(gameScreenShot, null);
        saveImageAsPng(screenshotImage, screenShotFile);

        final BufferedImage bImageMouse = newBufferImage(screenshotImage);
        final BufferedImage bImageGaze = newBufferImage(screenshotImage);
        final BufferedImage bImageMouseAndGaze = newBufferImage(screenshotImage);

        Graphics gMouse = initGazeMetricsImage(bImageMouse, screenshotImage);
        Graphics gGaze = initGazeMetricsImage(bImageGaze, screenshotImage);
        Graphics gMouseAndGaze = initGazeMetricsImage(bImageMouseAndGaze, screenshotImage);

        try (BufferedWriter bf = Files.newBufferedWriter(replayDataFile.toPath(), Charset.defaultCharset())) {
            bf.write(buildSavedDataJSON(coordinateData).toString());
            bf.flush();
        }

        final SavedStatsInfo savedStatsInfo = new SavedStatsInfo(heatMapCsvFile, gazeMetricsFileMouse, gazeMetricsFileGaze, gazeMetricsFileMouseAndGaze, screenShotFile,
            colorBandsFile, replayDataFile);

        this.savedStatsInfo = savedStatsInfo;
        if (this.heatMap != null) {
            final HeatMap hm = new HeatMap(heatMap, config.getHeatMapOpacity(), config.getHeatMapColors());
            addHeatMapToMetrics(hm, bImageMouse, gMouse, screenshotImage);
            addHeatMapToMetrics(hm, bImageGaze, gGaze, screenshotImage);
            addHeatMapToMetrics(hm, bImageMouseAndGaze, gMouseAndGaze, screenshotImage);
            saveHeatMapAsCsv(heatMapCsvFile);
        }

        if (this.fixationSequence != null) {
            addFixationSequence(FixationSequence.MOUSE_FIXATION_SEQUENCE, gMouse, gMouseAndGaze, screenshotImage);
            addFixationSequence(FixationSequence.GAZE_FIXATION_SEQUENCE, gGaze, gMouseAndGaze, screenshotImage);
        }

        saveImageAsPng(bImageMouse, gazeMetricsFileMouse);
        saveImageAsPng(bImageGaze, gazeMetricsFileGaze);
        saveImageAsPng(bImageMouseAndGaze, gazeMetricsFileMouseAndGaze);

        savedStatsInfo.notifyFilesReady();
        return savedStatsInfo;
    }

    public RoundsDurationReport getRoundsDurationReport() { return roundsDurationReport;}

    public long computeRoundsDurationAverageDuration() {
        return roundsDurationReport.computeAverageLength();
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long computeRoundsDurationMedianDuration() {
        return roundsDurationReport.computeMedianDuration();
    }

    public long getRoundsTotalAdditiveDuration() {
        return roundsDurationReport.getTotalAdditiveDuration();
    }

    public long computeTotalElapsedDuration() {
        return lifeCycle.computeTotalElapsedDuration();
    }

    public double computeRoundsDurationVariance() {
        return roundsDurationReport.computeVariance();
    }

    public double computeRoundsDurationStandardDeviation() {
        return roundsDurationReport.computeSD();
    }

    public void incrementNumberOfGoalsToReach() {
        if (!inReplayMode) {
            nbGoalsToReach++;
            currentRoundStartTime = System.currentTimeMillis();
            log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
        }
    }

    public void incrementNumberOfGoalsToReach(int i) {
        if (!inReplayMode) {
            nbGoalsToReach += i;
            currentRoundStartTime = System.currentTimeMillis();
            log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
        }
    }

    public void incrementNumberOfGoalsReached() {
        if (!inReplayMode) {
            final long currentRoundEndTime = System.currentTimeMillis();
            final long currentRoundDuration = currentRoundEndTime - currentRoundStartTime;
            if (currentRoundDuration < accidentalShotPreventionPeriod) {
                nbUnCountedGoalsReached++;
            } else {
                nbGoalsReached++;
                this.roundsDurationReport.addRoundDuration(currentRoundDuration);
            }
            currentRoundStartTime = currentRoundEndTime;
            log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
        }
    }

    public void addRoundDuration() {
        this.roundsDurationReport.addRoundDuration(System.currentTimeMillis() - currentRoundStartTime);
    }

    public int getShotRatio() {
        if (this.nbGoalsToReach == this.nbGoalsReached || this.nbGoalsToReach == 0) {
            return 100;
        } else {
            return (int) ((float) this.nbGoalsReached / (float) this.nbGoalsToReach * 100.0);
        }
    }

    public List<Long> getSortedDurationsBetweenGoals() {
        return this.roundsDurationReport.getSortedDurationsBetweenGoals();
    }

    public List<Long> getOriginalDurationsBetweenGoals() {
        return this.roundsDurationReport.getOriginalDurationsBetweenGoals();
    }

    public String getDirectoryOfVideo() {
        return nameOfVideo;
    }

    protected File createInfoStatsFile() {
        final File outputDirectory = getGameStatsOfTheDayDirectory();

        final String fileName = DateUtils.dateTimeNow() + "-info-game.csv";
        return new File(outputDirectory, fileName);
    }

    public File getGameStatsOfTheDayDirectory() {
        final File statsDirectory = GazePlayDirectories.getUserStatsFolder(ActiveConfigurationContext.getInstance().getUserName());
        final File gameDirectory = new File(statsDirectory, gameName);
        final File todayDirectory = new File(gameDirectory, DateUtils.today());
        final boolean outputDirectoryCreated = todayDirectory.mkdirs();
        log.info("outputDirectoryCreated = {}", outputDirectoryCreated);

        return todayDirectory;
    }

    protected void printLengthBetweenGoalsToString(final PrintWriter out) {
        this.roundsDurationReport.printLengthBetweenGoalsToString(out);
    }

    private void saveHeatMapAsCsv(final File file) throws IOException {
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            for (final double[] doubles : heatMap) {
                for (int j = 0; j < heatMap[0].length - 1; j++) {
                    out.print((int) doubles[j]);
                    out.print(", ");
                }
                out.print((int) doubles[doubles.length - 1]);
                out.println("");
            }
        }
    }

    void incrementFixationSequence(final int x, final int y, LinkedList<FixationPoint> fixationSequence) {

        final long gazeDuration;

        final FixationPoint newGazePoint = new FixationPoint(System.currentTimeMillis(), 0, y, x);
        if (fixationSequence.size() != 0) {
            gazeDuration = newGazePoint.getTimeGaze()
                - (fixationSequence.get(fixationSequence.size() - 1)).getTimeGaze();
            newGazePoint.setGazeDuration(gazeDuration);
        }

        // if the new points coordinates are the same as last one's in the list then update the last fixationPoint in
        // the list
        // same coordinate points are a result of the eyetracker's frequency of sampling
        if (fixationSequence.size() > 1
            && (Math.abs(newGazePoint.getX()
            - fixationSequence.get(fixationSequence.size() - 1).getX()) <= fixationTrail)
            && (Math.abs(newGazePoint.getY()
            - fixationSequence.get(fixationSequence.size() - 1).getY()) <= fixationTrail)) {
            fixationSequence.get(fixationSequence.size() - 1)
                .setGazeDuration(newGazePoint.getGazeDuration() + newGazePoint.getGazeDuration());
        } else { // else add the new point in the list
            fixationSequence.add(newGazePoint);
        }
    }

    void incrementHeatMap(final int x, final int y) {
        currentGazeTime = System.currentTimeMillis();
        // in heatChart, x and y are opposed
        final int newX = (int) (y / heatMapPixelSize);
        final int newY = (int) (x / heatMapPixelSize);
        for (int i = -trail; i <= trail; i++) {
            for (int j = -trail; j <= trail; j++) {
                if (Math.sqrt(i * i + j * j) < trail) {
                    increment(newX + i, newY + j);
                }
            }
        }
    }

    private void increment(final int x, final int y) {
        if (heatMap != null && x >= 0 && y >= 0 && x < heatMap.length && y < heatMap[0].length) {
            heatMap[x][y]++;
        }
    }

    /**
     * @return the size of the HeatMap Pixel Size in order to avoid a too big heatmap (400 px) if maximum memory is more
     * than 1Gb, only 200
     */
    double computeHeatMapPixelSize(final Scene gameContextScene) {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final double width = gameContextScene.getWidth();
        final double result;
        if (maxMemory < 1024 * 1024 * 1024) {
            // size is less than 1Gb (2^30)
            result = width / 200;
        } else {
            result = width / 400;
        }
        log.info("computeHeatMapPixelSize() : result = {}", result);
        return result;
    }

    public void takeScreenShot() {
        if (!inReplayMode) {
            gameScreenShot = gameContextScene.snapshot(null);
        }
    }

    private JsonObject buildSavedDataJSON(JsonArray data) {
        Gson gson = new GsonBuilder().create();
        JsonArray fixationSequenceArray = gson.toJsonTree(fixationSequence).getAsJsonArray();
        JsonArray durationBetweenGoalsArray = gson.toJsonTree(roundsDurationReport.getDurationBetweenGoals()).getAsJsonArray();
        String screenAspectRatio = getScreenRatio();
        double sceneAspectRatio = getSceneRatio();

        savedDataObj.addProperty("gameSeed", currentGameSeed);
        savedDataObj.addProperty("gameName", currentGameNameCode);
        savedDataObj.addProperty("gameVariant", currentGameVariant);
        savedDataObj.addProperty("gameStartedTime", startTime);
        savedDataObj.addProperty("screenAspectRatio", screenAspectRatio);
        savedDataObj.addProperty("sceneAspectRatio", sceneAspectRatio);
        savedDataObj.addProperty("statsNbGoalsReached", nbGoalsReached);
        savedDataObj.addProperty("statsNbGoalsToReach", nbGoalsToReach);
        savedDataObj.addProperty("statsNbUnCountedGoalsReached", nbUnCountedGoalsReached);

        JsonObject lifeCycleObject = new JsonObject();
        lifeCycleObject.addProperty("startTime", lifeCycle.getStartTime());
        lifeCycleObject.addProperty("stopTime", lifeCycle.getStopTime());
        savedDataObj.add("lifeCycle", lifeCycleObject);

        JsonObject roundsDurationReportObject = new JsonObject();
        roundsDurationReportObject.addProperty("totalAdditiveDuration", roundsDurationReport.getTotalAdditiveDuration());
        roundsDurationReportObject.add("durationBetweenGoals", durationBetweenGoalsArray);
        savedDataObj.add("roundsDurationReport", roundsDurationReportObject);

        savedDataObj.add("fixationSequence", fixationSequenceArray);
        savedDataObj.add("coordinatesAndTimeStamp", data);
        return savedDataObj;
    }

    private JsonArray saveCoordinates(JsonObject coordinates) {
        coordinateData.add(coordinates);
        return coordinateData;
    }

    public int greatestCommonFactor(int width, int height) {
        return (height == 0) ? width : greatestCommonFactor(height, width % height);
    }

    String getScreenRatio() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int screenWidth = gd.getDisplayMode().getWidth();
        int screenHeight = gd.getDisplayMode().getHeight();

        int factor = greatestCommonFactor(screenWidth, screenHeight);

        int widthRatio = screenWidth / factor;
        int heightRatio = screenHeight / factor;
        return widthRatio + ":" + heightRatio;
    }

    double getSceneRatio() {
        return gameContextScene.getHeight() / gameContextScene.getWidth();
    }

    public void setGameVariant(String gameVariant, String gameNameCode) {
        currentGameVariant = gameVariant;
        currentGameNameCode = gameNameCode;
    }

    public void setGameSeed(double gameSeed) {
        currentGameSeed = gameSeed;
    }

    public String getCurrentGameVariant(){
        return currentGameVariant;
    }

    public LevelsReport getLevelsReport() {
        return levelsReport;
    }

    public List<Long> getLevelsRounds() {
        return this.levelsReport.getOriginalLevelsPerRounds();
    }

    public String getCurrentGameNameCode() {
        return this.currentGameNameCode;
    }

    public ChiReport getChiReport() {
        return this.chiReport;
    }

    public static void setConfigMenuOpen(boolean configMenuStatus) {
        configMenuOpen = configMenuStatus;
    }

    public void takeScreenshotWithThread(){
        if (System.currentTimeMillis() - currentTimeMillisScreenshot > 500){
            takeScreenShot();
            currentTimeMillisScreenshot = System.currentTimeMillis();
            Thread threadScreenshot = new Thread(() -> {
               final File todayDirectory = getGameStatsOfTheDayDirectory();
               final String now = DateUtils.dateTimeNow();
               final String nowMillis = Long.toString(currentTimeMillisScreenshot).substring(10);
               final String screenShotFilePrefix = now + nowMillis + "-screenshot";
               final File screenShotFile = new File(todayDirectory, screenShotFilePrefix + ".png");
               final BufferedImage screenshotImage = SwingFXUtils.fromFXImage(gameScreenShot, null);
               saveImageAsPng(screenshotImage, screenShotFile);
            });
            threadScreenshot.start();
        }
    }
}
