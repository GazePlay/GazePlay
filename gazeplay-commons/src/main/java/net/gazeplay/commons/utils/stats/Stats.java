package net.gazeplay.commons.utils.stats;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by schwab on 16/08/2017.
 */
@SuppressWarnings("ALL")
@Slf4j
@ToString
public class Stats implements GazeMotionListener {

    private static final int trail = 10;
    private static final int fixationTrail = 50;
    private final double heatMapPixelSize = computeHeatMapPixelSize();
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
    private List<CoordinatesTracker> movementHistory = new ArrayList<>();
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

    public Stats(Scene gameContextScene) {
        this(gameContextScene, null);
    }

    public Stats(Scene gameContextScene, String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;
    }

    private static double[][] instanciateHeatMapData(Scene gameContextScene, double heatMapPixelSize) {
        int heatMapWidth = (int) (gameContextScene.getHeight() / heatMapPixelSize);
        int heatMapHeight = (int) (gameContextScene.getWidth() / heatMapPixelSize);
        log.info("heatMapWidth = {}, heatMapHeight = {}", heatMapWidth, heatMapHeight);
        return new double[heatMapWidth][heatMapHeight];
    }

    public ArrayList<TargetAOI> getTargetAOIList() {
        return this.targetAOIList;
    }

    public void setTargetAOIList(ArrayList<TargetAOI> targetAOIList) {

        this.targetAOIList = targetAOIList;
        for (int i = 0; i < targetAOIList.size() - 1; i++) {
            long duration = targetAOIList.get(i + 1).getTimeStarted() - targetAOIList.get(i).getTimeStarted();
            this.targetAOIList.get(i).setDuration(duration);
            System.out.println("The duration is " + duration);
        }
        if (targetAOIList.size() >= 1)
            targetAOIList.get(targetAOIList.size() - 1).setDuration(0);

    }

    public void notifyNewRoundReady() {
        currentRoundStartTime = System.currentTimeMillis();
        takeScreenShot();
    }

    public void startVideoRecording() {
        directoryOfVideo = getGameStatsOfTheDayDirectory().toString();
        this.movieFolder = new File(directoryOfVideo);
        float quality = 1.0F;
        byte bitDepth = 24;

        String mimeType;
        String videoFormatName;
        String compressorName;

        mimeType = "video/avi";
        videoFormatName = "tscc";
        compressorName = "Techsmith Screen Capture";
        ScreenRecorderCompactMain asi = null;
        GraphicsConfiguration cfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();
        Rectangle areaRect = null;
        Dimension outputDimension = null;
        areaRect = cfg.getBounds();

        outputDimension = areaRect.getSize();
        byte screenRate;
        screenRate = 30;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");
            nameOfVideo = this.movieFolder + "/ScreenRecording " + dateFormat.format(new Date());
            System.out.println("The name of the video is " + nameOfVideo);
            this.screenRecorder = new ScreenRecorder(cfg, areaRect,
                    new Format(VideoFormatKeys.MediaTypeKey, FormatKeys.MediaType.FILE, VideoFormatKeys.MimeTypeKey,
                            mimeType),
                    new Format(VideoFormatKeys.MediaTypeKey, FormatKeys.MediaType.VIDEO, VideoFormatKeys.EncodingKey,
                            videoFormatName, VideoFormatKeys.CompressorNameKey, compressorName,
                            VideoFormatKeys.WidthKey, outputDimension.width, VideoFormatKeys.HeightKey,
                            outputDimension.height, VideoFormatKeys.DepthKey, (int) bitDepth,
                            VideoFormatKeys.FrameRateKey, Rational.valueOf((double) screenRate),
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
        this.screenRecorder = null;
        (new Worker() {
            protected Object construct() throws Exception {
                r.stop();
                return null;
            }

            protected void finished() {
                ScreenRecorder.State state = r.getState();
                // File source;
                // File target;
                // try {
                // source = new File(nameOfVideo + ".avi");
                // target = new File(nameOfVideo + ".mp4");
                // VideoAttributes videoAttributes = new VideoAttributes();
                // videoAttributes.setCodec("mpeg4");
                // EncodingAttributes attrs = new EncodingAttributes();
                // attrs.setFormat("mp4");
                // attrs.setVideoAttributes(videoAttributes);
                // Encoder encoder = new Encoder();
                // encoder.encode(new MultimediaObject(source), target, attrs);
                // } catch (Exception ex) {
                // ex.printStackTrace();
                // }
            }
        }).start();
    }

    public void start() {
        Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isVideoRecordingEnabled()) {
            startVideoRecording();
        }
        lifeCycle.start(() -> {
            if (!config.isHeatMapDisabled())
                heatMap = instanciateHeatMapData(gameContextScene, heatMapPixelSize);
            if (!config.isFixationSequenceDisabled())
                fixationSequence = new LinkedList<FixationPoint>();
            startTime = System.currentTimeMillis();
            recordGazeMovements = e -> {
                int getX = (int) e.getX();
                int getY = (int) e.getY();
                if (!config.isHeatMapDisabled())
                    incHeatMap(getX, getY);
                if (!config.isFixationSequenceDisabled()) {
                    incFixationSequence(getX, getY);
                }
                if (config.isAreaOfInterestEnabled()) {
                    if (getX != previousX || getY != previousY) {
                        long timeToFixation = System.currentTimeMillis() - startTime;
                        previousX = getX;
                        previousY = getY;
                        long timeInterval = (timeToFixation - previousTime);
                        movementHistory
                                .add(new CoordinatesTracker(getX, getY, timeInterval, System.currentTimeMillis()));
                        previousTime = timeToFixation;
                    }
                }
            };
            recordMouseMovements = e -> {
                int getX = (int) e.getX();
                int getY = (int) e.getY();
                if (!config.isHeatMapDisabled())
                    incHeatMap(getX, getY);
                if (!config.isFixationSequenceDisabled()) {
                    incFixationSequence(getX, getY);
                }
                if (config.isAreaOfInterestEnabled()) {
                    if (getX != previousX || getY != previousY && counter == 2) {
                        long timeElapsedMillis = System.currentTimeMillis() - startTime;
                        previousX = getX;
                        previousY = getY;
                        long timeInterval = (timeElapsedMillis - previousTime);
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
        Configuration config = ActiveConfigurationContext.getInstance();
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
    public void gazeMoved(javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incHeatMap(positionX, positionY);
        incFixationSequence(positionX, positionY);
    }

    private void saveImageAsPng(BufferedImage bufferedImage, File outputFile) {
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SavedStatsInfo saveStats() throws IOException {
        Configuration config = ActiveConfigurationContext.getInstance();

        File todayDirectory = getGameStatsOfTheDayDirectory();
        final String heatmapFilePrefix = Utils.now() + "-heatmap";
        final String gazeMetricsFilePrefix = Utils.now() + "-metrics";
        final String screenShotFilePrefix = Utils.now() + "-screenshot";
        final String colorBandsFilePrefix = Utils.now() + "-colorBands";

        File gazeMetricsFile = new File(todayDirectory, gazeMetricsFilePrefix + ".png");
        File heatMapCsvFile = new File(todayDirectory, heatmapFilePrefix + ".csv");
        File screenShotFile = new File(todayDirectory, screenShotFilePrefix + ".png");
        File colorBandsFile = new File(todayDirectory, colorBandsFilePrefix + "png");

        BufferedImage screenshotImage = SwingFXUtils.fromFXImage(gameScreenShot, null);
        saveImageAsPng(screenshotImage, screenShotFile);

        BufferedImage bImage = new BufferedImage(
                screenshotImage.getWidth() + (heatMap != null ? screenshotImage.getWidth() / 20 + 10 : 0),
                screenshotImage.getHeight(), screenshotImage.getType());

        Graphics g = bImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
        g.drawImage(screenshotImage, 0, 0, null);

        SavedStatsInfo savedStatsInfo = new SavedStatsInfo(heatMapCsvFile, gazeMetricsFile, screenShotFile,
                colorBandsFile);

        this.savedStatsInfo = savedStatsInfo;
        if (this.heatMap != null) {
            HeatMap hm = new HeatMap(heatMap, config.getHeatMapOpacity(), config.getHeatMapColors());
            BufferedImage heatmapImage = SwingFXUtils.fromFXImage(hm.getImage(), null);
            Kernel kernel = new Kernel(3, 3,
                    new float[] { 1 / 16f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 4f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 16f });
            BufferedImageOp op = new ConvolveOp(kernel);
            heatmapImage = op.filter(heatmapImage, null);
            g.drawImage(heatmapImage, 0, 0, screenshotImage.getWidth(), screenshotImage.getHeight(), null);

            BufferedImage key = SwingFXUtils.fromFXImage(hm.getColorKey(bImage.getWidth() / 20, bImage.getHeight() / 2),
                    null);
            g.drawImage(key, bImage.getWidth() - key.getWidth(), (bImage.getHeight() - key.getHeight()) / 2, null);

            saveHeatMapAsCsv(heatMapCsvFile);
        }

        if (this.fixationSequence != null) {
            // set the gazeDuration of the last Fixation Point
            fixationSequence.get(fixationSequence.size() - 1)
                    .setGazeDuration(fixationSequence.get(fixationSequence.size() - 1).getTimeGaze()
                            - fixationSequence.get(fixationSequence.size() - 2).getTimeGaze());
            FixationSequence scanpath = new FixationSequence((int) gameContextScene.getWidth(),
                    (int) gameContextScene.getHeight(), fixationSequence);
            fixationSequence = scanpath.getSequence();
            BufferedImage seqImage = SwingFXUtils.fromFXImage(scanpath.getImage(), null);
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
            int ratioRate = (int) ((float) this.nbGoals / (float) this.nbShots * 100.0);
            return ratioRate;
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
        File outputDirectory = getGameStatsOfTheDayDirectory();

        final String fileName = Utils.now() + "-info-game.csv";
        System.out.println("The output directory is " + outputDirectory.toURI());
        return new File(outputDirectory, fileName);
    }

    protected File getGameStatsOfTheDayDirectory() {
        File statsDirectory = GazePlayDirectories.getUserStatsFolder(ActiveConfigurationContext.getInstance().getUserName());
        File gameDirectory = new File(statsDirectory, gameName);
        File todayDirectory = new File(gameDirectory, Utils.today());
        boolean outputDirectoryCreated = todayDirectory.mkdirs();
        log.info("outputDirectoryCreated = {}", outputDirectoryCreated);

        return todayDirectory;
    }

    protected void printLengthBetweenGoalsToString(PrintWriter out) {
        this.roundsDurationReport.printLengthBetweenGoalsToString(out);
    }

    private void saveHeatMapAsCsv(File file) throws IOException {
        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
            for (int i = 0; i < heatMap.length; i++) {
                for (int j = 0; j < heatMap[0].length - 1; j++) {
                    out.print((int) heatMap[i][j]);
                    out.print(", ");
                }
                out.print((int) heatMap[i][heatMap[i].length - 1]);
                out.println("");
            }
        }
    }

    private void saveFixationSequenceAsPng(File outputPngFile) {

        // log.info(String.format("Fixation-Sequence size: %3d X %3d",
        // (int) (gameContextScene.getWidth() / heatMapPixelSize),
        // (int) (gameContextScene.getHeight() / heatMapPixelSize)));

        // FixationSequence sequence = new FixationSequence((int) (gameContextScene.getWidth() / heatMapPixelSize),
        // (int) (gameContextScene.getHeight() / heatMapPixelSize), fixationSequence);
        FixationSequence scanpath = new FixationSequence((int) gameContextScene.getWidth(),
                (int) gameContextScene.getHeight(), fixationSequence);
        try {
            scanpath.saveToFile(outputPngFile);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void incFixationSequence(int X, int Y) {

        long previousGaze;
        long gazeDuration;

        // int x = (int) (Y / heatMapPixelSize);
        // int y = (int) (X / heatMapPixelSize);
        int x = Y;
        int y = X;
        FixationPoint newGazePoint = new FixationPoint(System.currentTimeMillis(), 0, x, y);
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
            long gDuration = fixationSequence.get(fixationSequence.size() - 1).getGazeDuration();
            fixationSequence.get(fixationSequence.size() - 1)
                    .setGazeDuration(newGazePoint.getGazeDuration() + newGazePoint.getGazeDuration()); //

        } else { // else add the new point in the list
            fixationSequence.add(newGazePoint);
        }
    }

    private void incHeatMap(int X, int Y) {
        currentGazeTime = System.currentTimeMillis();
        // in heatChart, x and y are opposed
        int x = (int) (Y / heatMapPixelSize);
        int y = (int) (X / heatMapPixelSize);
        for (int i = -trail; i <= trail; i++)
            for (int j = -trail; j <= trail; j++) {
                if (Math.sqrt(i * i + j * j) < trail) {
                    inc(x + i, y + j);
                }
            }
    }

    private void inc(int x, int y) {
        if (heatMap != null && x >= 0 && y >= 0 && x < heatMap.length && y < heatMap[0].length) {
            heatMap[x][y]++;
        }
    }

    /**
     * @return the size of the HeatMap Pixel Size in order to avoid a too big heatmap (400 px) if maximum memory is more
     *         than 1Gb, only 200
     */
    private double computeHeatMapPixelSize() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        double width = Screen.getPrimary().getBounds().getWidth();
        double result;
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
