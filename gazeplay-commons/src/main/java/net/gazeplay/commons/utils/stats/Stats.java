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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongSupplier;



/**
 * Statistics of a game: what is recorded while it is played, and the result files written at its end.
 * <p>
 * While a round is running (from {@link #start()} to {@link #stop()}), filters on the root of the scene record each
 * gaze sample and each mouse movement: raw samples (for replayData.json and the replay), heatmaps (gaze, mouse, both),
 * fixation sequences and areas of interest, as enabled in the settings. The games count their goals, mistakes and
 * rounds with the increment... and notify... methods. {@link #saveStats()} then writes the files of the round in
 * statistics/&lt;game&gt;/&lt;yyyy-MM-dd&gt;/ (see {@link ReplayDataFormat} for the versions of the format).
 * <p>
 * Games with their own counters extend this class (e.g. ShootGamesStats).
 * <p>
 * Created by schwab on 16/08/2017.
 */
@Slf4j
@ToString
public class Stats {

    private final double heatMapPixelSize;
    public final Scene gameContextScene;
    protected String gameName;

    long startTime;
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
    /**
     * Areas of interest, computed for each source separately (R13: until October 2026 the two were mixed).
     */
    @Getter
    private final AreaOfInterestRecording gazeAreasOfInterest = new AreaOfInterestRecording();
    @Getter
    private final AreaOfInterestRecording mouseAreasOfInterest = new AreaOfInterestRecording();

    private File movieFolder;
    private ScreenRecorder screenRecorder;
    private ArrayList<TargetAOI> targetAOIList = null;
    /**
     * Mouse and gaze together (heatmap.csv, metricsMouseAndGaze.png), then each source alone (format version 2: in
     * version 1 the three metrics images and heatmap.csv used the mixed matrix, R1).
     */
    private HeatMapGrid heatMapGrid;
    private HeatMapGrid gazeHeatMapGrid;
    private HeatMapGrid mouseHeatMapGrid;


    /**
     * Goals reached by the player (targets hit, right answers...).
     */
    @Getter
    public int nbGoalsReached = 0;

    /**
     * Goals proposed to the player.
     */
    @Getter
    protected int nbGoalsToReach = 0;

    /**
     * A goal reached sooner than this delay (ms) after the previous one is counted apart
     * ({@code nbUnCountedGoalsReached}), as accidental.
     */
    @Setter
    private long accidentalShotPreventionPeriod = 0;

    @Getter
    private int nbUnCountedGoalsReached;

    /**
     * Mistakes of the player; -1 for the games that do not count them.
     */
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

    /**
     * Completed when the screen recorder, stopped asynchronously by {@link #endVideoRecording()}, has finished writing
     * the video file.
     */
    private final CompletableFuture<Void> videoRecordingEnded = new CompletableFuture<>();

    private Long currentRoundStartTime;

    public String variantType = "";

    // Scores of the evaluations (GazePlay Eval, code kept but unreachable until the merge of GazePlayLearning)
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

    private final Configuration config = ActiveConfigurationContext.getInstance();
    // Static on purpose: the configuration menu belongs to the game screen, built before the games create their
    // Stats. One game per JVM: it is the state of the game screen of this process
    private static boolean configMenuOpen = false;

    /**
     * Source of the timestamps of the recorded samples and fixations. Replaced in tests to make them deterministic.
     */
    LongSupplier clock = System::currentTimeMillis;

    /**
     * Statistics of a game being played, whose name is set later by the subclass.
     */
    public Stats(final Scene gameContextScene) {
        this(gameContextScene, null);
    }

    /**
     * Statistics of a replayed game, read from its replayData.json.
     */
    public Stats(final Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        this(gameContextScene, null, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    /**
     * Statistics of a game being played.
     *
     * @param gameContextScene the scene of the game: its root receives the events to record
     * @param gameName         name of the game, also the name of its folder of statistics
     */
    public Stats(final Scene gameContextScene, final String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;
        this.inReplayMode = false;

        heatMapPixelSize = computeHeatMapPixelSize(gameContextScene);
    }

    /**
     * Statistics of a replayed game, read from its replayData.json: nothing is counted again.
     */
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

    /**
     * Sets the targets of the game, for the areas of interest screen; the duration of each target is computed from its
     * times (0 for the last one).
     */
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

    /**
     * To be called by a game when a new round is displayed: starts the timing of the round and takes the screenshot
     * drawn under the heatmaps.
     */
    public void notifyNewRoundReady() {
        currentRoundStartTime = System.currentTimeMillis();
        takeScreenShot();
    }

    /**
     * To be called by a game when a round ends: adds its duration.
     */
    public void notifyNextRound() {
        final long currentRoundEndTime = System.currentTimeMillis();
        final long currentRoundDuration = currentRoundEndTime - this.currentRoundStartTime;
        this.roundsDurationReport.addRoundDuration(currentRoundDuration);
        currentRoundStartTime = currentRoundEndTime;
    }

    /**
     * Starts recording the screen into an AVI file of the statistics folder of the day (VIDEO_RECORDING_ENABLED
     * setting). A failure is logged: the game goes on without video.
     */
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
            this.screenRecorder = new ScreenRecorder(cfg, areaRect, fileFormat, screenFormat, null, null, this.movieFolder);
            this.screenRecorder.start();
        } catch (IOException | AWTException e) {
            log.error("Cannot start the video recording of the game", e);
        }
        // Null when the recorder could not be created: this line used to throw a NullPointerException
        if (this.screenRecorder != null) {
            this.screenRecorder.setAudioMixer(null);
        }
    }

    /**
     * Stops the video recording in the background; {@link #awaitVideoRecordingEnd(long, TimeUnit)} waits for the end of
     * the writing.
     */
    public void endVideoRecording() {
        final ScreenRecorder recorder = this.screenRecorder;
        if (recorder == null) {
            // No recording (not started, or the recorder could not be created): nothing to wait for
            videoRecordingEnded.complete(null);
            return;
        }
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

    /**
     * Starts the round: starts the video recording if enabled, resets the recorded data, and puts on the root of the
     * scene the filters recording the gaze and the mouse.
     *
     * @throws LifeCycle.LifeCycleControlException when already started (use {@link #reset()} for a new round)
     */
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

                        // AREA_OF_INTEREST_DISABLED=true means enabled (misleading name of a persistent key)
                        if (config.isAreaOfInterestDisabled()) {
                            gazeAreasOfInterest.add(getX, getY, timeToFixation);
                        }
                    }
                }
            };

            recordMouseMovements = e -> {
                // Movements only: the button events repeated the position as additional samples (format version 2,
                // R1)
                if (!configMenuOpen && isMouseMovement(e)) {
                    final long timeElapsedMillis = clock.getAsLong() - startTime;
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
                            mouseAreasOfInterest.add(getX, getY, timeElapsedMillis);
                        }
                    }
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

    /**
     * Adds a raw sample to "coordinatesAndTimeStamp": X and Y as ratios of the size of the scene, the time since the
     * start of the round, and the source ("gaze" or "mouse").
     */
    private void setJSONCoordinates(long timeElapsedMillis, int getX, int getY, String event) {
        JsonObject coordinates = new JsonObject();
        coordinates.addProperty("X", getX / gameContextScene.getWidth());
        coordinates.addProperty("Y", getY / gameContextScene.getHeight());
        coordinates.addProperty("time", timeElapsedMillis);
        coordinates.addProperty("event", event);
        saveCoordinates(coordinates);
    }

    /**
     * Starts a new round: the counters, the durations and the recorded data start again from zero.
     */
    public void reset() {
        nbGoalsReached = 0;
        nbMistakes = -1;
        nbGoalsToReach = 0;
        accidentalShotPreventionPeriod = 0;

        roundsDurationReport = new RoundsDurationReport();
        lifeCycle = new LifeCycle();
        start();
    }

    /**
     * Ends the round: stops the video recording if enabled, and the recording of the samples.
     */
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

    /**
     * Records a gaze position directly, without going through the gaze events: only used by the tests.
     */
    void gazeMoved(final javafx.geometry.Point2D position) {
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

    /**
     * Draws the screenshot of the game, on a black background, into an image of the metrics.
     *
     * @return the graphics of the image, to draw the heatmap and the fixation sequence on it
     */
    public Graphics initGazeMetricsImage(BufferedImage bImage, BufferedImage screenshotImage) {

        final Graphics graphics = bImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
        graphics.drawImage(screenshotImage, 0, 0, null);

        return graphics;
    }

    /**
     * @return an empty metrics image: the size of the screenshot, plus a band on the right for the legend of the
     *   heatmap
     */
    private BufferedImage newBufferImage(BufferedImage screenshotImage) {
        return new BufferedImage(
            screenshotImage.getWidth() + (heatMapGrid != null ? screenshotImage.getWidth() / 20 + 10 : 0),
            screenshotImage.getHeight(), screenshotImage.getType());
    }


    /**
     * Draws a heatmap, smoothed, over the screenshot of a metrics image, and its legend on the right.
     */
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

    /**
     * Draws a fixation sequence on its metrics image and on the image of both sources, and keeps the reduced sequence
     * (the one written in replayData.json).
     */
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

    /**
     * Writes the files of the round in the statistics folder of the day, all named after the current time: the
     * screenshot, replayData.json, the heatmaps as CSV (heatmap, heatmapGaze, heatmapMouse) and the metrics images
     * (metricsMouse, metricsGaze, metricsMouseAndGaze).
     *
     * @return the files written
     */
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

    /**
     * @return the average duration of the rounds, in ms
     */
    public long computeRoundsDurationAverageDuration() {
        return roundsDurationReport.computeAverageLength();
    }

    /**
     * @return the start time of the round (System.currentTimeMillis())
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * @return the median duration of the rounds, in ms
     */
    public long computeRoundsDurationMedianDuration() {
        return roundsDurationReport.computeMedianDuration();
    }

    /**
     * @return the sum of the durations of the rounds, in ms
     */
    public long getRoundsTotalAdditiveDuration() {
        return roundsDurationReport.getTotalAdditiveDuration();
    }

    /**
     * @return the duration of the game, from its start to its stop (or to now), in ms
     */
    public long computeTotalElapsedDuration() {
        return lifeCycle.computeTotalElapsedDuration();
    }

    /**
     * @return the standard deviation of the durations of the rounds, in ms
     */
    public double computeRoundsDurationStandardDeviation() {
        return roundsDurationReport.computeSD();
    }

    /**
     * Adds a goal to reach (not during a replay), and starts the timing of the next goal.
     */
    public void incrementNumberOfGoalsToReach() {
        if (!inReplayMode) {
            nbGoalsToReach++;
            currentRoundStartTime = System.currentTimeMillis();
            log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
        }
    }

    /**
     * Adds {@code i} goals to reach (not during a replay), and starts the timing of the next goal.
     */
    public void incrementNumberOfGoalsToReach(int i) {
        if (!inReplayMode) {
            nbGoalsToReach += i;
            currentRoundStartTime = System.currentTimeMillis();
            log.debug("The number of goals is " + nbGoalsToReach + "and the number shots is " + nbGoalsReached);
        }
    }

    /**
     * Counts a goal reached (not during a replay), with its duration; or as accidental when it comes sooner than the
     * accidental shot prevention period.
     */
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

    /**
     * Counts a mistake (not during a replay).
     */
    public void incrementNumberOfMistakes() {
        if (!inReplayMode) {
            nbMistakes++;
            log.debug("The number of mistakes is " + nbMistakes);
        }
    }

    /**
     * Adds the time since the start of the current round as a round duration.
     */
    public void addRoundDuration() {
        this.roundsDurationReport.addRoundDuration(System.currentTimeMillis() - currentRoundStartTime);
    }

    /**
     * @return the percentage of the goals reached among the goals to reach; 100 when there was no goal
     */
    public int getShotRatio() {
        if (this.nbGoalsToReach == this.nbGoalsReached || this.nbGoalsToReach == 0) {
            return 100;
        } else {
            return (int) ((float) this.nbGoalsReached / (float) this.nbGoalsToReach * 100.0);
        }
    }

    /**
     * @return the durations of the rounds, ordered for the chart of the statistics screen
     */
    public List<Long> getSortedDurationsBetweenGoals() {
        return this.roundsDurationReport.getSortedDurationsBetweenGoals();
    }

    /**
     * @return the durations of the rounds, in the order they were played
     */
    public List<Long> getOriginalDurationsBetweenGoals() {
        return this.roundsDurationReport.getOriginalDurationsBetweenGoals();
    }

    /**
     * @return a new "...-info-game.csv" file of the statistics folder of the day, named after the current time
     */
    protected File createInfoStatsFile() {
        final File outputDirectory = getGameStatsOfTheDayDirectory();

        final String fileName = DateUtils.dateTimeNow() + "-info-game.csv";
        return new File(outputDirectory, fileName);
    }

    /**
     * @return the statistics folder of the game for today (statistics/&lt;game&gt;/&lt;yyyy-MM-dd&gt;, in the folder of
     *   the current profile), created if missing
     */
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

    /**
     * Adds a sample, at the current time, to a fixation sequence.
     */
    void incrementFixationSequence(final int x, final int y, LinkedList<FixationPoint> fixationSequence) {
        incrementFixationSequence(x, y, clock.getAsLong(), fixationSequence);
    }

    private void incrementFixationSequence(final int x, final int y, final long time, LinkedList<FixationPoint> fixationSequence) {
        FixationSequenceRecorder.add(fixationSequence, time, x, y);
    }

    /**
     * Adds a sample to the heatmap of its source and to the one of both sources.
     */
    void incrementHeatMap(final int x, final int y, final boolean gaze) {
        currentGazeTime = System.currentTimeMillis();
        if (heatMapGrid != null) {
            heatMapGrid.add(x, y);
            (gaze ? gazeHeatMapGrid : mouseHeatMapGrid).add(x, y);
        }
    }

    /**
     * @return the raw heatmap matrix of mouse and gaze (rows follow the Y axis, columns the X axis), null if the
     *   heatmap is disabled
     */
    double[][] getHeatMapData() {
        return heatMapGrid == null ? null : heatMapGrid.counts();
    }

    /**
     * @return the raw heatmap matrix of the gaze or of the mouse, null if the heatmap is disabled
     */
    double[][] getHeatMapData(final boolean gaze) {
        final HeatMapGrid grid = gaze ? gazeHeatMapGrid : mouseHeatMapGrid;
        return grid == null ? null : grid.counts();
    }

    /**
     * Number of columns of the heatmaps (heatmap*.csv, metrics*.png), whatever the machine (format version 4).
     */
    static final int HEAT_MAP_COLUMNS = 400;

    /**
     * @return the size of a cell of the heatmaps: the width of the scene divided into {@link #HEAT_MAP_COLUMNS}. Before
     *   format version 4 it depended on the memory of the JVM (R7): 400 columns from 1 GiB, 200 below (a game started
     *   from the menu gets a quarter of the memory of the machine, so 200 columns on a machine with less than 4 GB)
     */
    double computeHeatMapPixelSize(final Scene gameContextScene) {
        final double result = gameContextScene.getWidth() / HEAT_MAP_COLUMNS;
        log.info("computeHeatMapPixelSize() : result = {}", result);
        return result;
    }

    /**
     * Takes the screenshot of the scene drawn under the heatmaps (not during a replay).
     */
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

    /**
     * @return the greatest common divisor of two numbers (Euclid's algorithm)
     */
    public int greatestCommonFactor(int width, int height) {
        return (height == 0) ? width : greatestCommonFactor(height, width % height);
    }

    /**
     * @return the aspect ratio of the main screen, e.g. "16:9"
     */
    String getScreenRatio() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int screenWidth = gd.getDisplayMode().getWidth();
        int screenHeight = gd.getDisplayMode().getHeight();

        int factor = greatestCommonFactor(screenWidth, screenHeight);

        int widthRatio = screenWidth / factor;
        int heightRatio = screenHeight / factor;
        return widthRatio + ":" + heightRatio;
    }

    /**
     * @return the height of the scene divided by its width
     */
    double getSceneRatio() {
        return gameContextScene.getHeight() / gameContextScene.getWidth();
    }

    /**
     * Sets the variant and the name of the game, written in replayData.json for the replay.
     */
    public void setGameVariant(String gameVariant, String gameNameCode) {
        currentGameVariant = gameVariant;
        currentGameNameCode = gameNameCode;
    }

    /**
     * Sets the seed of the random generator of the game, written in replayData.json for the replay.
     */
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

    /**
     * Tells whether the configuration menu of the game screen is open: the mouse is not recorded while it is, since it
     * is then used for the menu.
     */
    public static void setConfigMenuOpen(boolean configMenuStatus) {
        configMenuOpen = configMenuStatus;
    }
}
