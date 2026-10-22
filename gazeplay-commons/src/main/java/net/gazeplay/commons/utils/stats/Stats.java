package net.gazeplay.commons.utils.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongSupplier;



/**
 * Created by schwab on 16/08/2017.
 */
@Slf4j
@ToString
public class Stats implements GazeMotionListener {

    private final double heatMapPixelSize;
    public final Scene gameContextScene;
    protected String gameName;

    long startTime;
    int sceneCounter = 0;
    private EventHandler<MouseEvent> recordMouseMovements;
    private EventHandler<GazeEvent> recordGazeMovements;
    /**
     * Node on which the recording filters are registered, so that they are removed from the same node.
     */
    private Parent recordingRoot;
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
    /**
     * Mouse and gaze together (heatmap.csv, metricsMouseAndGaze.png), then each source alone (format version 2: in
     * version 1 the three metrics images and heatmap.csv used the mixed matrix, R1).
     */
    private HeatMapGrid heatMapGrid;
    private HeatMapGrid gazeHeatMapGrid;
    private HeatMapGrid mouseHeatMapGrid;

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
    private int nbMistakes = -1;

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
    String currentGameVariant;
    String currentGameNameCode;
    double currentGameSeed = 0.;
    private final boolean inReplayMode;

    private String directoryOfVideo;

    private String nameOfVideo;

    /**
     * Completed when the screen recorder, stopped asynchronously by {@link #endVideoRecording()}, has finished writing
     * the video file.
     */
    private final CompletableFuture<Void> videoRecordingEnded = new CompletableFuture<>();

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
    private final AreaOfInterestGrouper aoiGrouper = new AreaOfInterestGrouper();
    @Getter
    private final List<List<CoordinatesTracker>> allAOIListTemp = new ArrayList<>();
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

    // Static on purpose: the configuration menu belongs to the game screen, built before the games create their
    // Stats. One game per JVM: it is the state of the game screen of this process
    private static boolean configMenuOpen = false;

    /**
     * Source of the timestamps of the recorded samples and fixations. Replaced in tests to make them deterministic.
     */
    LongSupplier clock = System::currentTimeMillis;

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
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss");
            nameOfVideo = this.movieFolder + "/ScreenRecording " + dateFormat.format(new Date());
            this.screenRecorder = new ScreenRecorder(cfg, areaRect, fileFormat, screenFormat, null, null, this.movieFolder);
            this.screenRecorder.start();
        } catch (IOException | AWTException e) {
            log.error("Cannot start the video recording of the game", e);
        }
        this.screenRecorder.setAudioMixer(null);
    }

    public void endVideoRecording() {
        final ScreenRecorder recorder = this.screenRecorder;
        (new Worker<>() {
            @Override
            protected Object construct() throws Exception {
                try {
                    recorder.stop();
                } finally {
                    videoRecordingEnded.complete(null);
                }
                return null;
            }

            @Override
            protected void finished() {
            }
        }).start();
    }

    /**
     * Waits until the video file is completely written (the recorder is stopped asynchronously).
     *
     * @return true if the recording ended within the delay
     */
    public boolean awaitVideoRecordingEnd(final long timeout, final TimeUnit unit) {
        try {
            videoRecordingEnded.get(timeout, unit);
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final ExecutionException | TimeoutException e) {
            return false;
        }
    }

    /**
     * The video file is named by the screen recorder itself ("ScreenRecording yyyy-MM-dd 'at' HH.mm.ss.avi"). The
     * name used to be rebuilt by GazePlay with another time format (HH-mm-ss), which never matched: the video of the
     * areas of interest could not be shown.
     *
     * @return the video file written by the screen recorder, empty if there was no recording
     */
    public Optional<File> getVideoFile() {
        final ScreenRecorder recorder = this.screenRecorder;
        if (recorder == null) {
            return Optional.empty();
        }
        final List<File> files = recorder.getCreatedMovieFiles();
        return files.isEmpty() ? Optional.empty() : Optional.of(files.get(files.size() - 1));
    }

    private void generateAOIList(final int index) {
        aoiGrouper.add(movementHistory, index).ifPresent(area -> {
            allAOIListTemp.add(area.samples());
            startAndEndIdx.add(new int[]{area.start(), area.end()});
            allAOIListPolygonPt.add(area.rectangle());

            final Polygon areaOfInterest = new Polygon();
            areaOfInterest.getPoints().addAll(area.rectangle());
            colorIterator = index % 7;
            areaOfInterest.setStroke(colors[colorIterator]);
            allAOIListPolygon.add(areaOfInterest);
        });
    }

    public void start() {

        final Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isVideoRecordingEnabled()) {
            startVideoRecording();
        }
        lifeCycle.start(() -> {
            // reset() calls start() again: the filters of the previous round must not stay registered, otherwise
            // every sample is recorded once more per round (anomaly R4)
            removeRecordingFilters();
            if (!config.isHeatMapDisabled()) {
                heatMapGrid = new HeatMapGrid(gameContextScene.getWidth(), gameContextScene.getHeight(), heatMapPixelSize);
                gazeHeatMapGrid = new HeatMapGrid(gameContextScene.getWidth(), gameContextScene.getHeight(), heatMapPixelSize);
                mouseHeatMapGrid = new HeatMapGrid(gameContextScene.getWidth(), gameContextScene.getHeight(), heatMapPixelSize);
            }
            // The raw samples of replayData.json are those of the round, like the heatmap and the fixation sequence
            // (format version 2: in version 1 they also held the previous rounds, R5)
            coordinateData = new JsonArray();
            if (!config.isFixationSequenceDisabled()) {
                fixationSequence = new ArrayList<LinkedList<FixationPoint>>(List.of(new LinkedList<FixationPoint>(), new LinkedList<FixationPoint>()));
            }
            startTime = clock.getAsLong();

            recordGazeMovements = e -> {
                if (e.getSource() == gameContextScene.getRoot() && e.getTarget() == gameContextScene.getRoot()) {
                    // Format version 3: time of the acquisition of the sample, not of its handling on the JavaFX
                    // thread (samples queued while it was busy got the same time, and the time of the next ones)
                    final long sampleTime = e.getSampleTime() > 0 ? e.getSampleTime() : clock.getAsLong();
                    final long timeToFixation = sampleTime - startTime;
                    final long timeInterval = (timeToFixation - previousTime);
                    Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                    final int getX = (int) toSceneCoordinate.getX();
                    final int getY = (int) toSceneCoordinate.getY();
                    // Samples acquired before the start of the round (still queued) belong to the previous one
                    if (getX > 0 && getY > 0 && timeToFixation >= 0) {

                        setJSONCoordinates(timeToFixation, getX, getY, "gaze");

                        if (!config.isHeatMapDisabled()) {
                            incrementHeatMap(getX, getY, true);
                        }
                        if (!config.isFixationSequenceDisabled()) {
                            incrementFixationSequence(getX, getY, sampleTime, fixationSequence.get(FixationSequence.GAZE_FIXATION_SEQUENCE));
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
                // Movements only: the button events repeated the position as additional samples (format version 2,
                // R1)
                if (!configMenuOpen && isMouseMovement(e)) {
                    final long timeElapsedMillis = clock.getAsLong() - startTime;
                    final long timeInterval = (timeElapsedMillis - previousTime);
                    Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                    final int getX = (int) toSceneCoordinate.getX();
                    final int getY = (int) toSceneCoordinate.getY();
                    if (getX > 0 || getY > 0) {

                        setJSONCoordinates(timeElapsedMillis, getX, getY, "mouse");

                        if (!config.isHeatMapDisabled()) {
                            incrementHeatMap(getX, getY, false);
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

            recordingRoot = gameContextScene.getRoot();
            recordingRoot.addEventFilter(GazeEvent.ANY, recordGazeMovements);
            recordingRoot.addEventFilter(MouseEvent.ANY, recordMouseMovements);

        });
        currentRoundStartTime = lifeCycle.getStartTime();
    }

    private static boolean isMouseMovement(final MouseEvent e) {
        return e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == MouseEvent.MOUSE_DRAGGED;
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
        nbMistakes = -1;
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
        lifeCycle.stop(this::removeRecordingFilters);
    }

    /**
     * The filters used to be removed from the scene while they had been added to its root, which had no effect: the
     * samples kept being recorded after stop(), and once more per reset() (anomaly R4).
     */
    private void removeRecordingFilters() {
        if (recordingRoot == null) {
            return;
        }
        if (recordGazeMovements != null) {
            recordingRoot.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
        }
        if (recordMouseMovements != null) {
            recordingRoot.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
        }
        recordingRoot = null;
    }

    // function used for testing purposes
    @Override
    public void gazeMoved(final javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incrementHeatMap(positionX, positionY, true);
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
            screenshotImage.getWidth() + (heatMapGrid != null ? screenshotImage.getWidth() / 20 + 10 : 0),
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
        final String gazeHeatmapFilePrefix = now + "-heatmapGaze";
        final String mouseHeatmapFilePrefix = now + "-heatmapMouse";
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
        final File gazeHeatMapCsvFile = new File(todayDirectory, gazeHeatmapFilePrefix + ".csv");
        final File mouseHeatMapCsvFile = new File(todayDirectory, mouseHeatmapFilePrefix + ".csv");
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
        if (this.heatMapGrid != null) {
            final HeatMap hm = new HeatMap(heatMapGrid.counts(), config.getHeatMapOpacity(), config.getHeatMapColors());
            addHeatMapToMetrics(new HeatMap(mouseHeatMapGrid.counts(), config.getHeatMapOpacity(), config.getHeatMapColors()),
                bImageMouse, gMouse, screenshotImage);
            addHeatMapToMetrics(new HeatMap(gazeHeatMapGrid.counts(), config.getHeatMapOpacity(), config.getHeatMapColors()),
                bImageGaze, gGaze, screenshotImage);
            addHeatMapToMetrics(hm, bImageMouseAndGaze, gMouseAndGaze, screenshotImage);
            saveHeatMapAsCsv(heatMapGrid, heatMapCsvFile);
            saveHeatMapAsCsv(gazeHeatMapGrid, gazeHeatMapCsvFile);
            saveHeatMapAsCsv(mouseHeatMapGrid, mouseHeatMapCsvFile);
        }

        if (this.fixationSequence != null) {
            addFixationSequence(FixationSequence.MOUSE_FIXATION_SEQUENCE, gMouse, gMouseAndGaze, screenshotImage);
            addFixationSequence(FixationSequence.GAZE_FIXATION_SEQUENCE, gGaze, gMouseAndGaze, screenshotImage);
        }

        saveImageAsPng(bImageMouse, gazeMetricsFileMouse);
        saveImageAsPng(bImageGaze, gazeMetricsFileGaze);
        saveImageAsPng(bImageMouseAndGaze, gazeMetricsFileMouseAndGaze);
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

    public void incrementNumberOfMistakes() {
        if (!inReplayMode) {
            nbMistakes++;
            log.debug("The number of mistakes is " + nbMistakes);
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

    /**
     * @deprecated the name does not match the file written by the screen recorder, use {@link #getVideoFile()}
     */
    @Deprecated
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

    private static void saveHeatMapAsCsv(final HeatMapGrid grid, final File file) throws IOException {
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            grid.writeCsv(out);
        }
    }

    void incrementFixationSequence(final int x, final int y, LinkedList<FixationPoint> fixationSequence) {
        incrementFixationSequence(x, y, clock.getAsLong(), fixationSequence);
    }

    private void incrementFixationSequence(final int x, final int y, final long time, LinkedList<FixationPoint> fixationSequence) {
        FixationSequenceRecorder.add(fixationSequence, time, x, y);
    }

    void incrementHeatMap(final int x, final int y, final boolean gaze) {
        currentGazeTime = System.currentTimeMillis();
        if (heatMapGrid != null) {
            heatMapGrid.add(x, y);
            (gaze ? gazeHeatMapGrid : mouseHeatMapGrid).add(x, y);
        }
    }

    /**
     * @return the raw heatmap matrix of mouse and gaze (rows follow the Y axis, columns the X axis), null if the
     * heatmap is disabled
     */
    double[][] getHeatMapData() {
        return heatMapGrid == null ? null : heatMapGrid.counts();
    }

    double[][] getHeatMapData(final boolean gaze) {
        final HeatMapGrid grid = gaze ? gazeHeatMapGrid : mouseHeatMapGrid;
        return grid == null ? null : grid.counts();
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
        return ReplayDataWriter.toJson(new ReplayDataWriter.Round(
            currentGameSeed, currentGameNameCode, currentGameVariant, startTime, getScreenRatio(), getSceneRatio(),
            nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, nbMistakes,
            lifeCycle.getStartTime(), lifeCycle.getStopTime(),
            roundsDurationReport.getTotalAdditiveDuration(), roundsDurationReport.getDurationBetweenGoals(),
            fixationSequence, data));
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
