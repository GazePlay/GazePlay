package net.gazeplay.commons.utils.stats;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
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
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.VideoFormatKeys;
import org.monte.media.gui.Worker;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.monte.screenrecorder.ScreenRecorderCompactMain;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by schwab on 16/08/2017.
 */
@Slf4j
@ToString
public class Stats implements GazeMotionListener {

    private static final int trail = 10;
    private static final int fixationTrail = 50;
    private final double heatMapPixelSize;
    private final Scene gameContextScene;
    protected String gameName;
    @Getter
    protected int nbGoals = 0;
    long startTime;
    int sceneCounter = 0;
    private EventHandler<MouseEvent> recordMouseMovements;
    private EventHandler<GazeEvent> recordGazeMovements;
    private LifeCycle lifeCycle = new LifeCycle();
    private RoundsDurationReport roundsDurationReport = new RoundsDurationReport();
    private Instant starts;
    private int counter = 0;
    private final List<CoordinatesTracker> movementHistory = new ArrayList<>();
    private long previousTime = 0;
    private int previousX = 0;
    private int previousY = 0;
    private File movieFolder;
    private int nbShots = 0;
    private boolean convexHULL = true;
    private ScreenRecorder screenRecorder;
    private ArrayList<TargetAOI> targetAOIList = null;
    @Setter
    private long accidentalShotPreventionPeriod = 0;
    @Getter
    private int nbUnCountedShots;
    private double[][] heatMap;
    @Getter
    @Setter
    private long currentGazeTime;
    @Getter
    @Setter
    private long lastGazeTime;
    @Getter
    private LinkedList<FixationPoint> fixationSequence;
    @Getter
    private SavedStatsInfo savedStatsInfo;
    private WritableImage gameScreenShot;

    private String directoryOfVideo;

    private String nameOfVideo;

    private Long currentRoundStartTime;

    public Stats(final Scene gameContextScene) {
        this(gameContextScene, null);
    }

    public Stats(final Scene gameContextScene, final String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;

        heatMapPixelSize = computeHeatMapPixelSize(gameContextScene);
    }

    private static double[][] instanciateHeatMapData(final Scene gameContextScene, final double heatMapPixelSize) {
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
            final long duration = targetAOIList.get(i + 1).getTimeStarted() - targetAOIList.get(i).getTimeStarted();
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
        final ScreenRecorderCompactMain asi = null;

        System.setProperty("java.awt.headless", "false");
        final GraphicsConfiguration cfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();
        final Rectangle areaRect;
        final Dimension outputDimension;
        areaRect = cfg.getBounds();

        outputDimension = areaRect.getSize();
        final byte screenRate;
        screenRate = 30;
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");
            nameOfVideo = this.movieFolder + "/ScreenRecording " + dateFormat.format(new Date());
            this.screenRecorder = new ScreenRecorder(cfg, areaRect,
                new Format(VideoFormatKeys.MediaTypeKey, FormatKeys.MediaType.FILE, VideoFormatKeys.MimeTypeKey,
                    mimeType),
                new Format(VideoFormatKeys.MediaTypeKey, FormatKeys.MediaType.VIDEO, VideoFormatKeys.EncodingKey,
                    videoFormatName, VideoFormatKeys.CompressorNameKey, compressorName,
                    VideoFormatKeys.WidthKey, outputDimension.width, VideoFormatKeys.HeightKey,
                    outputDimension.height, VideoFormatKeys.DepthKey, (int) bitDepth,
                    VideoFormatKeys.FrameRateKey, Rational.valueOf(screenRate),
                    VideoFormatKeys.QualityKey, quality, VideoFormatKeys.KeyFrameIntervalKey, screenRate * 60),
                null, null, this.movieFolder);
            this.screenRecorder.start();
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
        this.screenRecorder.setAudioMixer(null);
    }

    public void endVideoRecording() {
        final ScreenRecorder r = this.screenRecorder;
        (new Worker() {
            @Override
            protected Object construct() throws Exception {
                r.stop();
                return null;
            }

            @Override
            protected void finished() {
            }
        }).start();
    }

    public void start() {
        final Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isVideoRecordingEnabled()) {
            startVideoRecording();
        }
        lifeCycle.start(() -> {
            if (!config.isHeatMapDisabled()) {
                heatMap = instanciateHeatMapData(gameContextScene, heatMapPixelSize);
            }
            if (!config.isFixationSequenceDisabled()) {
                fixationSequence = new LinkedList<>();
            }
            startTime = System.currentTimeMillis();
            recordGazeMovements = e -> {
                final int getX = (int) e.getX();
                final int getY = (int) e.getY();
                if (!config.isHeatMapDisabled()) {
                    incHeatMap(getX, getY);
                }
                if (!config.isFixationSequenceDisabled()) {
                    incFixationSequence(getX, getY);
                }
                if (config.getAreaOfInterestDisabledProperty().getValue()) {
                    if (getX != previousX || getY != previousY) {
                        final long timeToFixation = System.currentTimeMillis() - startTime;
                        previousX = getX;
                        previousY = getY;
                        final long timeInterval = (timeToFixation - previousTime);
                        movementHistory
                            .add(new CoordinatesTracker(getX, getY, timeInterval, System.currentTimeMillis()));
                        previousTime = timeToFixation;
                    }
                }
            };
            recordMouseMovements = e -> {
                final int getX = (int) e.getX();
                final int getY = (int) e.getY();
                if (!config.isHeatMapDisabled()) {
                    incHeatMap(getX, getY);
                }
                if (!config.isFixationSequenceDisabled()) {
                    incFixationSequence(getX, getY);
                }
                if (config.getAreaOfInterestDisabledProperty().getValue()) {
                    if (getX != previousX || getY != previousY && counter == 2) {
                        final long timeElapsedMillis = System.currentTimeMillis() - startTime;
                        previousX = getX;
                        previousY = getY;
                        final long timeInterval = (timeElapsedMillis - previousTime);
                        movementHistory
                            .add(new CoordinatesTracker(getX, getY, timeInterval, System.currentTimeMillis()));
                        previousTime = timeElapsedMillis;
                        counter = 0;
                    }
                    counter++;
                }
            };
            gameContextScene.addEventFilter(GazeEvent.ANY, recordGazeMovements);
            gameContextScene.addEventFilter(MouseEvent.ANY, recordMouseMovements);
            takeScreenShot();

        });
        currentRoundStartTime = lifeCycle.getStartTime();

    }

    public List<CoordinatesTracker> getMovementHistoryWithTime() {
        return this.movementHistory;
    }

    public void reset() {
        nbShots = 0;
        nbGoals = 0;
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

    @Override
    public void gazeMoved(final javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incHeatMap(positionX, positionY);
        incFixationSequence(positionX, positionY);
    }

    private void saveImageAsPng(final BufferedImage bufferedImage, final File outputFile) {
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SavedStatsInfo saveStats() throws IOException {
        final Configuration config = ActiveConfigurationContext.getInstance();

        final File todayDirectory = getGameStatsOfTheDayDirectory();
        final String heatmapFilePrefix = Utils.now() + "-heatmap";
        final String gazeMetricsFilePrefix = Utils.now() + "-metrics";
        final String screenShotFilePrefix = Utils.now() + "-screenshot";
        final String colorBandsFilePrefix = Utils.now() + "-colorBands";

        final File gazeMetricsFile = new File(todayDirectory, gazeMetricsFilePrefix + ".png");
        final File heatMapCsvFile = new File(todayDirectory, heatmapFilePrefix + ".csv");
        final File screenShotFile = new File(todayDirectory, screenShotFilePrefix + ".png");
        final File colorBandsFile = new File(todayDirectory, colorBandsFilePrefix + "png");

        final BufferedImage screenshotImage = SwingFXUtils.fromFXImage(gameScreenShot, null);
        saveImageAsPng(screenshotImage, screenShotFile);

        final BufferedImage bImage = new BufferedImage(
            screenshotImage.getWidth() + (heatMap != null ? screenshotImage.getWidth() / 20 + 10 : 0),
            screenshotImage.getHeight(), screenshotImage.getType());

        final Graphics g = bImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
        g.drawImage(screenshotImage, 0, 0, null);

        final SavedStatsInfo savedStatsInfo = new SavedStatsInfo(heatMapCsvFile, gazeMetricsFile, screenShotFile,
            colorBandsFile);

        this.savedStatsInfo = savedStatsInfo;
        if (this.heatMap != null) {
            final HeatMap hm = new HeatMap(heatMap, config.getHeatMapOpacity(), config.getHeatMapColors());
            BufferedImage heatmapImage = SwingFXUtils.fromFXImage(hm.getImage(), null);
            final Kernel kernel = new Kernel(3, 3,
                new float[]{1 / 16f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 4f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 16f});
            final BufferedImageOp op = new ConvolveOp(kernel);
            heatmapImage = op.filter(heatmapImage, null);
            g.drawImage(heatmapImage, 0, 0, screenshotImage.getWidth(), screenshotImage.getHeight(), null);

            final BufferedImage key = SwingFXUtils.fromFXImage(hm.getColorKey(bImage.getWidth() / 20, bImage.getHeight() / 2),
                null);
            g.drawImage(key, bImage.getWidth() - key.getWidth(), (bImage.getHeight() - key.getHeight()) / 2, null);

            saveHeatMapAsCsv(heatMapCsvFile);
        }

        if (this.fixationSequence != null) {
            // set the gazeDuration of the last Fixation Point
            fixationSequence.get(fixationSequence.size() - 1)
                .setGazeDuration(fixationSequence.get(fixationSequence.size() - 1).getTimeGaze()
                    - fixationSequence.get(fixationSequence.size() - 2).getTimeGaze());
            final FixationSequence scanpath = new FixationSequence((int) gameContextScene.getWidth(),
                (int) gameContextScene.getHeight(), fixationSequence);
            fixationSequence = scanpath.getSequence();
            final BufferedImage seqImage = SwingFXUtils.fromFXImage(scanpath.getImage(), null);
            g.drawImage(seqImage, 0, 0, screenshotImage.getWidth(), screenshotImage.getHeight(), null);
        }
        // for (FixationPoint p : fixationSequence) {
        // log.info("x = {}, y = {}, fGaze= {}, gDuration={}",p.getY(), p.getX(), p.getFirstGaze(),
        // p.getGazeDuration());
        // }

        saveImageAsPng(bImage, gazeMetricsFile);

        savedStatsInfo.notifyFilesReady();
        return savedStatsInfo;
    }

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

    public void incNbGoals() {
        final long currentRoundEndTime = System.currentTimeMillis();
        final long currentRoundDuration = currentRoundEndTime - currentRoundStartTime;
        if (currentRoundDuration < accidentalShotPreventionPeriod) {
            nbUnCountedShots++;
        } else {
            nbGoals++;
            this.roundsDurationReport.addRoundDuration(currentRoundDuration);
        }
        currentRoundStartTime = currentRoundEndTime;
        log.debug("The number of goals is " + nbGoals + "and the number shots is " + nbShots);
    }

    public void addRoundDuration() {
        this.roundsDurationReport.addRoundDuration(System.currentTimeMillis() - currentRoundStartTime);
    }

    public int getShotRatio() {
        if (this.nbGoals == this.nbShots || this.nbShots == 0) {
            return 100;
        } else {
            return (int) ((float) this.nbGoals / (float) this.nbShots * 100.0);
        }
    }

    public void incNbShots() {
        this.nbShots++;
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

        final String fileName = Utils.now() + "-info-game.csv";
        return new File(outputDirectory, fileName);
    }

    protected File getGameStatsOfTheDayDirectory() {
        final File statsDirectory = GazePlayDirectories.getUserStatsFolder(ActiveConfigurationContext.getInstance().getUserName());
        final File gameDirectory = new File(statsDirectory, gameName);
        final File todayDirectory = new File(gameDirectory, Utils.today());
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

    private void saveFixationSequenceAsPng(final File outputPngFile) {

        // log.info(String.format("Fixation-Sequence size: %3d X %3d",
        // (int) (gameContextScene.getWidth() / heatMapPixelSize),
        // (int) (gameContextScene.getHeight() / heatMapPixelSize)));

        // FixationSequence sequence = new FixationSequence((int) (gameContextScene.getWidth() / heatMapPixelSize),
        // (int) (gameContextScene.getHeight() / heatMapPixelSize), fixationSequence);
        final FixationSequence scanpath = new FixationSequence((int) gameContextScene.getWidth(),
            (int) gameContextScene.getHeight(), fixationSequence);
        try {
            scanpath.saveToFile(outputPngFile);
        } catch (final Exception e) {
            log.error("Exception", e);
        }
    }

    private void incFixationSequence(final int X, final int Y) {

        long previousGaze;
        final long gazeDuration;

        // int x = (int) (Y / heatMapPixelSize);
        // int y = (int) (X / heatMapPixelSize);
        final FixationPoint newGazePoint = new FixationPoint(System.currentTimeMillis(), 0, Y, X);
        if (fixationSequence.size() != 0) {
            gazeDuration = newGazePoint.getTimeGaze()
                - (fixationSequence.get(fixationSequence.size() - 1)).getTimeGaze();
            newGazePoint.setGazeDuration(gazeDuration);
        }

        /*
         * if (fixationSequence.size() == 0) { newGazePoint = new FixationPoint(0, 0, x, y);
         * fixationSequence.add(newGazePoint); } else { newGazePoint = new FixationPoint(System.currentTimeMillis() -
         * startTime, 0, x, y); gazeDuration = newGazePoint.getTimeGaze() -
         * (fixationSequence.get(fixationSequence.size() - 1)).getTimeGaze();
         * (fixationSequence.get(fixationSequence.size() - 1)).setGazeDuration(gazeDuration); }
         */
        // if the new points coordinates are the same as last one's in the list then update the last fixationPoint in
        // the list
        // same coordinate points are a result of the eyetracker's frequency of sampling
        if (fixationSequence.size() > 1
            && (Math.abs(newGazePoint.getX()
            - fixationSequence.get(fixationSequence.size() - 1).getX()) <= fixationTrail)
            && (Math.abs(newGazePoint.getY()
            - fixationSequence.get(fixationSequence.size() - 1).getY()) <= fixationTrail)) {
            fixationSequence.get(fixationSequence.size() - 1)
                .setGazeDuration(newGazePoint.getGazeDuration() + newGazePoint.getGazeDuration()); //

        } else { // else add the new point in the list
            fixationSequence.add(newGazePoint);
        }
    }

    private void incHeatMap(final int X, final int Y) {
        currentGazeTime = System.currentTimeMillis();
        // in heatChart, x and y are opposed
        final int x = (int) (Y / heatMapPixelSize);
        final int y = (int) (X / heatMapPixelSize);
        for (int i = -trail; i <= trail; i++) {
            for (int j = -trail; j <= trail; j++) {
                if (Math.sqrt(i * i + j * j) < trail) {
                    inc(x + i, y + j);
                }
            }
        }
    }

    private void inc(final int x, final int y) {
        if (heatMap != null && x >= 0 && y >= 0 && x < heatMap.length && y < heatMap[0].length) {
            heatMap[x][y]++;
        }
    }

    /**
     * @return the size of the HeatMap Pixel Size in order to avoid a too big heatmap (400 px) if maximum memory is more
     * than 1Gb, only 200
     */
    private double computeHeatMapPixelSize(final Scene gameContextScene) {
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

    private void takeScreenShot() {
        gameScreenShot = gameContextScene.snapshot(null);
    }

    public WritableImage getGameScreenShot() {
        return this.gameScreenShot;
    }
}
