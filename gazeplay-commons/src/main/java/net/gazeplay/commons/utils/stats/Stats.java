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
import javafx.scene.paint.Color;
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
import org.jetbrains.annotations.NotNull;
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

    @Getter
    long startTime;
    @Getter
    private RoundsDurationReport roundsDurationReport = new RoundsDurationReport();
    private Long currentRoundStartTime;

    int sceneCounter = 0;
    private int counter = 0;
    private EventHandler<MouseEvent> recordMouseMovements;
    private EventHandler<GazeEvent> recordGazeMovements;
    private LifeCycle lifeCycle = new LifeCycle();

    @Getter
    private final LevelsReport levelsReport = new LevelsReport();
    @Getter
    private final ChiReport chiReport = new ChiReport();

    @Getter
    @Setter
    private List<CoordinatesTracker> movementHistory = new ArrayList<>();

    private long previousTime = 0;
    private int previousXMouse = 0;
    private int previousYMouse = 0;
    private int previousXGaze = 0;
    private int previousYGaze = 0;

    private File movieFolder;
    private final boolean convexHULL = true;
    private ScreenRecorder screenRecorder;
    private double[][] heatMap;

    @Getter
    private List<TargetAOI> targetAOIList = null;

    @Getter
    public int nbGoalsReached = 0;
    @Getter
    protected int nbGoalsToReach = 0;
    @Getter
    private int nbUncountedGoalsReached;

    @Setter
    private long accidentalShotPreventionPeriod = 0;

    @Getter
    @Setter
    private long currentGazeTime;
    @Getter
    @Setter
    private long lastGazeTime;

    @Getter
    private List<List<FixationPoint>> fixationSequence;

    @Getter
    private SavedStatsInfo savedStatsInfo;

    @Getter
    private WritableImage gameScreenShot;

    @Getter
    String currentGameVariant;
    @Getter
    String currentGameNameCode;
    @Setter
    double currentGameSeed = 0.;

    private String directoryOfVideo;
    private String nameOfVideo;

    public String variantType = "";

    // Phonology
    public int totalPhonology = 0;
    public int simpleScoreItemsPhonology = 0;
    public int complexScoreItemsPhonology = 0;
    public int scoreLeftTargetItemsPhonology = 0;
    public int scoreRightTargetItemsPhonology = 0;

    // Semantics
    public int totalSemantic = 0;
    public int simpleScoreItemsSemantic = 0;
    public int complexScoreItemsSemantic = 0;
    public int frequentScoreItemSemantic = 0;
    public int infrequentScoreItemSemantic = 0;
    public int scoreLeftTargetItemsSemantic = 0;
    public int scoreRightTargetItemsSemantic = 0;

    // Morphosyntax
    public int totalMorphosyntax = 0;
    public int simpleScoreItemsMorphosyntax = 0;
    public int complexScoreItemsMorphosyntax = 0;
    public int scoreLeftTargetItemsMorphosyntax = 0;
    public int scoreRightTargetItemsMorphosyntax = 0;

    // Word comprehension
    public int totalWordComprehension = 0;
    public int totalItemsAddedManually = 0;

    public int total = 0;
    public long timeGame = 0;
    public String actualFile = "";

    // Parameters for AOI
    @Getter
    private double highestFixationTime = 0;
    @Getter
    private List<AreaOfInterest> AOIList = new ArrayList<>();

    private int movementHistoryIdx = 0;
    private List<CoordinatesTracker> AOITrackerList = new ArrayList<>();
    private final List<List<CoordinatesTracker>> AOITempList = new ArrayList<>();
    private final List<int[]> AOIStartEndIdxList = new ArrayList<>();
    private final List<Polygon> AOIPolygonList = new ArrayList<>();
    private final List<Double[]> AOIPolygonPtList = new ArrayList<>();

    private final Configuration config = ActiveConfigurationContext.getInstance();

    @Setter
    private static boolean configMenuOpen = false;

    /* CONSTRUCTORS */

    public Stats(final Scene gameContextScene) {
        this(gameContextScene, null);
    }

    public Stats(final Scene gameContextScene,
                 int nbGoalsReached, int nbGoalsToReach, int nbUncountedGoalsReached,
                 LifeCycle lifeCycle,
                 RoundsDurationReport roundsDurationReport,
                 List<List<FixationPoint>> fixationSequence,
                 List<CoordinatesTracker> movementHistory,
                 double[][] heatMap,
                 List<AreaOfInterest> AOIList,
                 SavedStatsInfo savedStatsInfo
    ) {
        this(gameContextScene, null, nbGoalsReached, nbGoalsToReach, nbUncountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    public Stats(final Scene gameContextScene, final String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;

        heatMapPixelSize = computeHeatMapPixelSize(gameContextScene);
    }

    public Stats(final Scene gameContextScene,
                 final String gameName,
                 int nbGoalsReached, int nbGoalsToReach, int nbUncountedGoalsReached,
                 LifeCycle lifeCycle,
                 RoundsDurationReport roundsDurationReport,
                 List<List<FixationPoint>> fixationSequence,
                 List<CoordinatesTracker> movementHistory,
                 double[][] heatMap,
                 List<AreaOfInterest> AOIList,
                 SavedStatsInfo savedStatsInfo
    ) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;
        this.nbGoalsReached = nbGoalsReached;
        this.nbGoalsToReach = nbGoalsToReach;
        this.nbUncountedGoalsReached = nbUncountedGoalsReached;
        this.lifeCycle = lifeCycle;
        this.roundsDurationReport = roundsDurationReport;
        this.fixationSequence = fixationSequence;
        this.movementHistory = movementHistory;
        this.heatMap = heatMap;
        this.AOIList = AOIList;
        this.savedStatsInfo = savedStatsInfo;

        heatMapPixelSize = computeHeatMapPixelSize(gameContextScene);
    }

    /**
     * @return the size of the HeatMap Pixel Size in order to avoid a too big heatmap (400 px)
     * if maximum memory is more than 1Gb, only 200
     */
    double computeHeatMapPixelSize(final Scene gameContextScene) {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final double width = gameContextScene.getWidth();
        final double result = width / ((maxMemory < 1024 * 1024 * 1024) ? 200 : 400); // size is less than 1Gb (2^30)
        log.info("computeHeatMapPixelSize() : result = {}", result);
        return result;
    }

    /* GETTERS AND SETTERS */

    String getScreenRatio() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int screenWidth = gd.getDisplayMode().getWidth();
        int screenHeight = gd.getDisplayMode().getHeight();

        int factor = getGreatestCommonFactor(screenWidth, screenHeight);

        int widthRatio = screenWidth / factor;
        int heightRatio = screenHeight / factor;
        return widthRatio + ":" + heightRatio;
    }

    double getSceneRatio() {
        return gameContextScene.getHeight() / gameContextScene.getWidth();
    }

    public int getShotRatio() {
        return (this.nbGoalsToReach == this.nbGoalsReached || this.nbGoalsToReach == 0) ?
            100 : (int) ((float) this.nbGoalsReached / (float) this.nbGoalsToReach * 100.0);
    }

    public List<Long> getLevelsRounds() {
        return levelsReport.getOriginalLevelsPerRounds();
    }

    public String getDirectoryOfVideo() {
        return nameOfVideo;
    }

    public File getGameStatsOfTheDayDirectory() {
        final File statsDirectory = GazePlayDirectories.getUserStatsFolder(ActiveConfigurationContext.getInstance().getUserName());
        final File gameDirectory = new File(statsDirectory, gameName);
        final File todayDirectory = new File(gameDirectory, DateUtils.today());
        final boolean outputDirectoryCreated = todayDirectory.mkdirs();
        log.info("outputDirectoryCreated = {}", outputDirectoryCreated);
        return todayDirectory;
    }

    public void setTargetAOIList(final List<TargetAOI> targetAOIList) {
        this.targetAOIList = targetAOIList;
        for (int i = 0; i < targetAOIList.size() - 1; i++) {
            final long duration = targetAOIList.get(i).getTimeEnded() - targetAOIList.get(i).getTimeStarted();
            this.targetAOIList.get(i).setDuration(duration);
        }
        if (targetAOIList.size() >= 1)
            targetAOIList.get(targetAOIList.size() - 1).setDuration(0);
    }

    public void setGameVariant(String gameVariant, String gameNameCode) {
        currentGameVariant = gameVariant;
        currentGameNameCode = gameNameCode;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public int getGreatestCommonFactor(int width, int height) {
        return (height == 0) ? width : getGreatestCommonFactor(height, width % height);
    }

    public long computeTotalElapsedDuration() {
        return lifeCycle.computeTotalElapsedDuration();
    }

    /* ROUND DURATION REPORT */

    public void addRoundDuration() {
        this.roundsDurationReport.addRoundDuration(System.currentTimeMillis() - currentRoundStartTime);
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

    public long computeRoundsDurationAverageDuration() {
        return roundsDurationReport.computeAverageLength();
    }

    public long computeRoundsDurationMedianDuration() {
        return roundsDurationReport.computeMedianDuration();
    }

    public double computeRoundsDurationStandardDeviation() {
        return roundsDurationReport.computeSD();
    }

    public long getRoundsTotalAdditiveDuration() {
        return roundsDurationReport.getTotalAdditiveDuration();
    }

    public List<Long> getSortedDurationsBetweenGoals() {
        return roundsDurationReport.getSortedDurationsBetweenGoals();
    }

    public List<Long> getOriginalDurationsBetweenGoals() {
        return roundsDurationReport.getOriginalDurationsBetweenGoals();
    }

    protected void printLengthBetweenGoalsToString(final PrintWriter out) {
        this.roundsDurationReport.printLengthBetweenGoalsToString(out);
    }

    void takeScreenShot() {
        gameScreenShot = gameContextScene.snapshot(null);
    }

    /* NUMBER OF GOALS */

    public void incrementNumberOfGoalsToReach() {
        nbGoalsToReach++;
        currentRoundStartTime = System.currentTimeMillis();
        log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
    }

    public void incrementNumberOfGoalsToReach(int i) {
        nbGoalsToReach += i;
        currentRoundStartTime = System.currentTimeMillis();
        log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
    }

    public void incrementNumberOfGoalsReached() {
        final long currentRoundEndTime = System.currentTimeMillis();
        final long currentRoundDuration = currentRoundEndTime - currentRoundStartTime;
        if (currentRoundDuration < accidentalShotPreventionPeriod) {
            nbUncountedGoalsReached++;
        } else {
            nbGoalsReached++;
            roundsDurationReport.addRoundDuration(currentRoundDuration);
        }
        currentRoundStartTime = currentRoundEndTime;
        log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
    }

    /* VIDEO RECORDING */

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

    /* STATS RECORDING */

    public void start() {
        final Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isVideoRecordingEnabled())
            startVideoRecording();

        lifeCycle.start(() -> {
            if (!config.isHeatMapDisabled())
                heatMap = instantiateHeatMapData(gameContextScene, heatMapPixelSize);
            if (!config.isFixationSequenceDisabled())
                fixationSequence = new ArrayList<>(List.of(new LinkedList<>(), new LinkedList<>()));
            startTime = System.currentTimeMillis();

            recordGazeMovements = e -> {
                if (e.getSource() == gameContextScene.getRoot() && e.getTarget() == gameContextScene.getRoot()) {
                    final long time = System.currentTimeMillis();
                    final long timeToFixation = time - startTime;
                    final long timeInterval = (timeToFixation - previousTime);
                    Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                    final int x = (int) toSceneCoordinate.getX();
                    final int y = (int) toSceneCoordinate.getY();
                    final double xValue = x / gameContextScene.getWidth();
                    final double yValue = y / gameContextScene.getHeight();

                    if (x > 0 && y > 0) {
                        if (!config.isHeatMapDisabled())
                            incrementHeatMap(x, y);
                        if (!config.isFixationSequenceDisabled())
                            incrementFixationSequence(x, y, fixationSequence.get(FixationSequence.GAZE_FIXATION_SEQUENCE));

                        if (config.getAreaOfInterestDisabledProperty().getValue()) {
                            if (x != previousXMouse || y != previousYMouse) {
                                previousXMouse = x;
                                previousYMouse = y;
                                movementHistory.add(new CoordinatesTracker(xValue, yValue, timeInterval, time, "gaze"));
                                movementHistoryIdx++;
                                if (movementHistoryIdx > 1)
                                    generateAOIList(movementHistoryIdx - 1);
                                previousTime = timeToFixation;
                            }
                        }
                    }
                }
            };

            recordMouseMovements = e -> {
                if (!configMenuOpen) {
                    final long time = System.currentTimeMillis();
                    final long timeElapsed = time - startTime;
                    final long timeInterval = (timeElapsed - previousTime);
                    Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                    final int x = (int) toSceneCoordinate.getX();
                    final int y = (int) toSceneCoordinate.getY();
                    final double xValue = x / gameContextScene.getWidth();
                    final double yValue = y / gameContextScene.getHeight();

                    if (x > 0 || y > 0) {
                        if (!config.isHeatMapDisabled())
                            incrementHeatMap(x, y);
                        if (!config.isFixationSequenceDisabled())
                            incrementFixationSequence(x, y, fixationSequence.get(FixationSequence.MOUSE_FIXATION_SEQUENCE));

                        if (config.getAreaOfInterestDisabledProperty().getValue()) {
                            if (x != previousXGaze || y != previousYGaze && counter == 2) {
                                previousXGaze = x;
                                previousYGaze = y;
                                movementHistory.add(new CoordinatesTracker(xValue, yValue, timeInterval, time, "mouse"));
                                movementHistoryIdx++;
                                if (movementHistoryIdx > 1)
                                    generateAOIList(movementHistoryIdx - 1);
                                previousTime = timeElapsed;
                                counter = 0;
                            }
                            counter++;
                        }
                    }
                }
            };

            gameContextScene.getRoot().addEventFilter(GazeEvent.ANY, recordGazeMovements);
            gameContextScene.getRoot().addEventFilter(MouseEvent.ANY, recordMouseMovements);

        });
        currentRoundStartTime = lifeCycle.getStartTime();
    }

    static double[][] instantiateHeatMapData(final Scene gameContextScene, final double heatMapPixelSize) {
        final int heatMapWidth = (int) (gameContextScene.getHeight() / heatMapPixelSize);
        final int heatMapHeight = (int) (gameContextScene.getWidth() / heatMapPixelSize);
        log.info("heatMapWidth = {}, heatMapHeight = {}", heatMapWidth, heatMapHeight);
        return new double[heatMapWidth][heatMapHeight];
    }

    private void generateAOIList(final int index) {
        final double sceneWidth = gameContextScene.getWidth();
        final double sceneHeight = gameContextScene.getHeight();
        final double x1 = movementHistory.get(index).getXValue() * sceneWidth;
        final double y1 = movementHistory.get(index).getYValue() * sceneHeight;
        final double x2 = movementHistory.get(index - 1).getXValue() * sceneWidth;
        final double y2 = movementHistory.get(index - 1).getYValue() * sceneHeight;
        final double eDistance = Math.sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));

        if (eDistance < 150 && movementHistory.get(index).getIntervalTime() > 10) {
            if (index == 1)
                AOITrackerList.add(movementHistory.get(0));
            AOITrackerList.add(movementHistory.get(index));
        } else {
            if (AOITrackerList.size() > 2) {
                AOITempList.add(new ArrayList<>(AOITrackerList));
                int[] startEnd = new int[]{index - AOITrackerList.size(), index};
                AOIStartEndIdxList.add(startEnd);
                final Point2D[] points = new Point2D[AOITrackerList.size()];

                for (int i = 0; i < AOITrackerList.size(); i++) {
                    CoordinatesTracker coordinate = AOITrackerList.get(i);
                    points[i] = new Point2D(coordinate.getXValue() * sceneWidth, coordinate.getYValue() * sceneHeight);
                }

                final boolean convexHull = config.getConvexHullDisabledProperty().getValue();
                final Double[] polygonPoints = convexHull ? calculateConvexHull(points) : calculateRectangle(points);
                AOIPolygonPtList.add(polygonPoints);
            } else if (eDistance > 700) {
                AOITrackerList.add(movementHistory.get(index));
                int[] startEnd = new int[]{index - AOITrackerList.size(), index};
                AOIStartEndIdxList.add(startEnd);
                AOITempList.add(new ArrayList<>(AOITrackerList));
                final float radius = 15;
                final Point2D[] points = new Point2D[8];

                for (int i = 0; i < 8; i++) {
                    CoordinatesTracker coordinate = AOITrackerList.get(0);
                    points[i] = new Point2D(coordinate.getXValue() * sceneWidth + pow(-1, i) * radius,
                        coordinate.getYValue() * sceneHeight + pow(-1, i) * radius);
                }

                final boolean convexHull = config.getConvexHullDisabledProperty().getValue();
                final Double[] polygonPoints = convexHull ? calculateConvexHull(points) : calculateRectangle(points);
                AOIPolygonPtList.add(polygonPoints);
            }
            AOITrackerList = new ArrayList<>();
        }
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
        if (config.isVideoRecordingEnabled())
            endVideoRecording();

        lifeCycle.stop(() -> {
            if (recordGazeMovements != null)
                gameContextScene.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
            if (recordMouseMovements != null)
                gameContextScene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
        });

        calculateMovementHistoryDistances();
        calculateAOIList();
        calculateAOIPriorities();
        if (targetAOIList != null)
            calculateTargetAOIList();
    }

    /* CALCULATE STATS */

    /**
     * Treating the data, post-processing to take performance constraint of during the data collection.
     */
    void calculateMovementHistoryDistances() {
        if (!movementHistory.isEmpty())
            movementHistory.get(0).setDistance(0);
        for (int i = 1; i < movementHistory.size(); i++) {
            final double sceneWidth = gameContextScene.getWidth();
            final double sceneHeight = gameContextScene.getHeight();
            final double x = Math.pow(movementHistory.get(i).getXValue() * sceneWidth - movementHistory.get(i - 1).getXValue() * sceneWidth, 2);
            final double y = Math.pow(movementHistory.get(i).getYValue() * sceneHeight - movementHistory.get(i - 1).getYValue() * sceneHeight, 2);
            final double distance = Math.sqrt(x + y);
            movementHistory.get(i).setDistance(distance);
        }
    }

    void calculateAOIList() {
        int i;
        for (i = 0; i < AOITempList.size(); i++) {
            final String AOINumber = "AOI number " + (AOIList.size() + 1);
            AOITrackerList = AOITempList.get(i);
            int centerX = 0;
            int centerY = 0;
            final int movementHistoryEndingIndex = AOIStartEndIdxList.get(i)[1];
            final int movementHistoryStartingIndex = AOIStartEndIdxList.get(i)[0];
            final long areaStartTime = AOITrackerList.get(0).getTimeStarted();
            final long areaEndTime = AOITrackerList.get(AOITrackerList.size() - 1).getTimeStarted()
                + AOITrackerList.get(AOITrackerList.size() - 1).getIntervalTime();
            final double timeSpent = (areaEndTime - areaStartTime) / 1000.0;
            final double ttff = (areaStartTime - startTime) / 1000.0;

            if (timeSpent > highestFixationTime)
                highestFixationTime = timeSpent;

            for (CoordinatesTracker coordinate : AOITrackerList) {
                centerX += coordinate.getXValue() * gameContextScene.getWidth();
                centerY += coordinate.getYValue() * gameContextScene.getHeight();
            }

            centerX /= AOITrackerList.size();
            centerY /= AOITrackerList.size();

            final AreaOfInterest areaOfInterest = new AreaOfInterest(AOINumber, AOITrackerList, centerX, centerY,
                AOIPolygonPtList.get(i), movementHistoryStartingIndex, movementHistoryEndingIndex, timeSpent, ttff);
            AOIList.add(areaOfInterest);
        }
    }

    void calculateAOIPriorities() {
        if (highestFixationTime != 0) {
            for (final AreaOfInterest areaOfInterest : AOIList) {
                final double priority = areaOfInterest.getTimeSpent() / highestFixationTime * 0.6 + 0.1;
                areaOfInterest.setPriority(priority);
            }
        }
    }

    /**
     * Creates a rectangle surrounding each Area Of Interest in the provided list.
     * The rectangle is added as a transparent {@code Polygon} to each {@code TargetAOI}.
     *
     * @see Polygon
     */
    void calculateTargetAOIList() {
        for (final TargetAOI targetAOI : targetAOIList) {
            log.debug("The target is at (" + targetAOI.getXValue() + ", " + targetAOI.getYValue() + ")");

            final int radius = targetAOI.getAreaRadius();
            final Point2D[] point2D = {
                new Point2D(targetAOI.getXValue() - 100, targetAOI.getYValue()),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() + 100),
                new Point2D(targetAOI.getXValue(), targetAOI.getYValue() - radius),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() - radius)
            };

            final Double[] polygonPoints;
            polygonPoints = calculateRectangle(point2D);

            final Polygon targetArea;
            targetArea = new Polygon();
            targetArea.getPoints().addAll(polygonPoints);
            targetArea.setFill(Color.rgb(255, 255, 255, 0.4));
            targetAOI.setPolygon(targetArea);
        }
    }

    /**
     * Calculates a rectangle that can enclose all the points provided, with a set padding around the points.
     *
     * @param points the array of {@code Point2D} objects to enclose
     * @return the {@code double} array containing all X and Y values of each rectangle point in sequence
     * @see Point2D
     */
    Double[] calculateRectangle(final Point2D @NotNull [] points) {
        double leftPoint = points[0].getX();
        double rightPoint = points[0].getX();
        double topPoint = points[0].getY();
        double bottomPoint = points[0].getY();

        for (int i = 1; i < points.length; i++) {
            if (points[i].getX() < leftPoint)
                leftPoint = points[i].getX();
            if (points[i].getX() > rightPoint)
                rightPoint = points[i].getX();
            if (points[i].getY() > topPoint)
                topPoint = points[i].getY();
            if (points[i].getY() < bottomPoint)
                bottomPoint = points[i].getY();
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

    /**
     * Implements Jarvis's Algorithm to calculate the points on a Convex Hull.
     * <p>
     * It starts by calculating the left-most point and will loop through all the points
     * until it reaches this point again, finding the most convex points.
     *
     * @param points the array of {@code Point2D} objects to calculate the hull around
     * @return the {@code double} array containing all X and Y values of each hull point in sequence
     * @see Point2D
     */
    Double[] calculateConvexHull(final Point2D @NotNull [] points) {
        final int numberOfPoints = points.length;
        final ArrayList<Double> convexHullPoints = new ArrayList<>();
        final Vector<Point2D> hull = new Vector<>();

        // Finding the index of the lowest X value, or left-most point, in all points.
        int lowestValueIndex = 0;
        for (int i = 1; i < numberOfPoints; i++)
            if (points[i].getX() < points[lowestValueIndex].getX())
                lowestValueIndex = i;

        int point = lowestValueIndex, q;
        do {
            hull.add(points[point]);
            q = (point + 1) % numberOfPoints;
            for (int i = 0; i < numberOfPoints; i++)
                if (orientation(points[point], points[i], points[q]) < 0) // Checking if the points are convex.
                    q = i;
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

    /**
     * Determines the orientation of an ordered triplet of 2D points on a plane.
     * The points provided can be collinear, clockwise or counterclockwise.
     *
     * @param p1 the first {@code Point2D}
     * @param p2 the second {@code Point2D}
     * @param p3 the third {@code Point2D}
     * @return the value {@code 0} if the points are collinear;
     * a value greater than {@code 0} if the points are clockwise;
     * and a value less than {@code 0} if the points are counterclockwise
     * @see Point2D
     */
    int orientation(final @NotNull Point2D p1, final @NotNull Point2D p2, final @NotNull Point2D p3) {
        final int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
            - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        return Integer.compare(val, 0);
    }

    /* GAZE MOVED */

    /**
     * Function used for testing purposes.
     */
    @Override
    public void gazeMoved(final @NotNull Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incrementHeatMap(positionX, positionY);
        incrementFixationSequence(positionX, positionY, fixationSequence.get(FixationSequence.GAZE_FIXATION_SEQUENCE));
    }

    void incrementHeatMap(final int x, final int y) {
        currentGazeTime = System.currentTimeMillis();
        // in heatChart, x and y are opposed
        final int newX = (int) (y / heatMapPixelSize);
        final int newY = (int) (x / heatMapPixelSize);
        for (int i = -trail; i <= trail; i++)
            for (int j = -trail; j <= trail; j++)
                if (Math.sqrt(i * i + j * j) < trail)
                    increment(newX + i, newY + j);
    }

    private void increment(final int x, final int y) {
        if (heatMap != null && x >= 0 && y >= 0 && x < heatMap.length && y < heatMap[0].length)
            heatMap[x][y]++;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    void incrementFixationSequence(final int x, final int y, List<FixationPoint> fixationSequence) {
        final long gazeDuration;

        final FixationPoint newGazePoint = new FixationPoint(System.currentTimeMillis(), 0, y, x);
        if (fixationSequence.size() != 0) {
            gazeDuration = newGazePoint.getTimeGaze() - (fixationSequence.get(fixationSequence.size() - 1)).getTimeGaze();
            newGazePoint.setGazeDuration(gazeDuration);
        }

        // if the new points coordinates are the same as last one's in the list then update the last fixationPoint in
        // the list
        // same coordinate points are a result of the eye-tracker's frequency of sampling
        if (fixationSequence.size() > 1 &&
            (Math.abs(newGazePoint.getX() - fixationSequence.get(fixationSequence.size() - 1).getX()) <= fixationTrail) &&
            (Math.abs(newGazePoint.getY() - fixationSequence.get(fixationSequence.size() - 1).getY()) <= fixationTrail)) {
            fixationSequence.get(fixationSequence.size() - 1)
                .setGazeDuration(newGazePoint.getGazeDuration() + newGazePoint.getGazeDuration());
        } else { // else add the new point in the list
            fixationSequence.add(newGazePoint);
        }
    }

    /* SAVE STATS */

    public SavedStatsInfo saveStats() throws IOException {
        final Configuration config = ActiveConfigurationContext.getInstance();

        final File todayDirectory = getGameStatsOfTheDayDirectory();
        final String now = DateUtils.dateTimeNow();
        final String gazeMetricsFilePrefixMouse = now + "-metricsMouse";
        final String gazeMetricsFilePrefixGaze = now + "-metricsGaze";
        final String gazeMetricsFilePrefixMouseAndGaze = now + "-metricsMouseAndGaze";
        final String screenShotFilePrefix = now + "-screenshot";
        final String colorBandsFilePrefix = now + "-colorBands";
        final String replayDataFilePrefix = now + "-replayData";

        final File gazeMetricsFileMouse = new File(todayDirectory, gazeMetricsFilePrefixMouse + ".png");
        final File gazeMetricsFileGaze = new File(todayDirectory, gazeMetricsFilePrefixGaze + ".png");
        final File gazeMetricsFileMouseAndGaze = new File(todayDirectory, gazeMetricsFilePrefixMouseAndGaze + ".png");
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
            bf.write(buildSavedDataJSON().toString());
            bf.flush();
        }

        savedStatsInfo = new SavedStatsInfo(gazeMetricsFileMouse, gazeMetricsFileGaze, gazeMetricsFileMouseAndGaze,
            screenShotFile, colorBandsFile, replayDataFile);

        if (this.heatMap != null) {
            final HeatMap hm = new HeatMap(heatMap, config.getHeatMapOpacity(), config.getHeatMapColors());
            addHeatMapToMetrics(hm, bImageMouse, gMouse, screenshotImage);
            addHeatMapToMetrics(hm, bImageGaze, gGaze, screenshotImage);
            addHeatMapToMetrics(hm, bImageMouseAndGaze, gMouseAndGaze, screenshotImage);
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

    static void saveImageAsPng(final BufferedImage bufferedImage, final File outputFile) {
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Graphics initGazeMetricsImage(BufferedImage bImage, BufferedImage screenshotImage) {
        final Graphics graphics = bImage.getGraphics();
        graphics.setColor(java.awt.Color.BLACK);
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

        final BufferedImage keyMouse = SwingFXUtils.fromFXImage(hm.getColorKey(bImage.getWidth() / 20, bImage.getHeight() / 2), null);

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

    protected File createInfoStatsFile() {
        final File outputDirectory = getGameStatsOfTheDayDirectory();
        final String fileName = DateUtils.dateTimeNow() + "-info-game.csv";
        return new File(outputDirectory, fileName);
    }

    private JsonObject buildSavedDataJSON() {
        final JsonObject savedDataObj = new JsonObject();

        Gson gson = new GsonBuilder().create();
        JsonArray fixationSequenceArray = gson.toJsonTree(fixationSequence).getAsJsonArray();
        JsonArray movementHistoryArray = gson.toJsonTree(movementHistory).getAsJsonArray();
        JsonArray heatMapArray = gson.toJsonTree(heatMap).getAsJsonArray();
        JsonArray AOIListArray = gson.toJsonTree(AOIList).getAsJsonArray();
        JsonArray durationBetweenGoalsArray = gson.toJsonTree(roundsDurationReport.getDurationBetweenGoals()).getAsJsonArray();

        JsonObject lifeCycleObject = new JsonObject();
        lifeCycleObject.addProperty("startTime", lifeCycle.getStartTime());
        lifeCycleObject.addProperty("stopTime", lifeCycle.getStopTime());

        JsonObject roundsDurationReportObject = new JsonObject();
        roundsDurationReportObject.addProperty("totalAdditiveDuration", roundsDurationReport.getTotalAdditiveDuration());
        roundsDurationReportObject.add("durationBetweenGoals", durationBetweenGoalsArray);

        savedDataObj.addProperty("gameSeed", currentGameSeed);
        savedDataObj.addProperty("gameName", currentGameNameCode);
        savedDataObj.addProperty("gameVariant", currentGameVariant);
        savedDataObj.addProperty("gameStartedTime", startTime);
        savedDataObj.addProperty("screenAspectRatio", getScreenRatio());
        savedDataObj.addProperty("sceneAspectRatio", getSceneRatio());
        savedDataObj.addProperty("statsNbGoalsReached", nbGoalsReached);
        savedDataObj.addProperty("statsNbGoalsToReach", nbGoalsToReach);
        savedDataObj.addProperty("statsNbUncountedGoalsReached", nbUncountedGoalsReached);
        savedDataObj.add("lifeCycle", lifeCycleObject);
        savedDataObj.add("roundsDurationReport", roundsDurationReportObject);
        savedDataObj.add("fixationSequence", fixationSequenceArray);
        savedDataObj.add("movementHistory", movementHistoryArray);
        savedDataObj.add("heatMap", heatMapArray);
        savedDataObj.add("AOIList", AOIListArray);

        return savedDataObj;
    }
}
