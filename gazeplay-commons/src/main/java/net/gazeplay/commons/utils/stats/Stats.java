package net.gazeplay.commons.utils.stats;

import com.github.agomezmoron.multimedia.recorder.VideoRecorder;
import com.github.agomezmoron.multimedia.recorder.configuration.VideoRecorderConfiguration;

import com.sun.javafx.PlatformUtil;
import javafx.application.Platform;
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
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.FixationSequence;
import net.gazeplay.commons.utils.HeatMap;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.games.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by schwab on 16/08/2017.
 */
@SuppressWarnings("ALL")
@Slf4j
@ToString
public class Stats implements GazeMotionListener {

    private Configuration config;
    private static final int trail = 10;
    private final double heatMapPixelSize = computeHeatMapPixelSize();
    private EventHandler<MouseEvent> recordMouseMovements;
    private EventHandler<GazeEvent> recordGazeMovements;
    private final Scene gameContextScene;
    private LifeCycle lifeCycle = new LifeCycle();
    private RoundsDurationReport roundsDurationReport = new RoundsDurationReport();
    protected String gameName;
    private Instant starts;
    private int counter = 0;
    private List<CoordinatesTracker> movementHistory = new ArrayList<>();
    private long previousTime = 0;
    private int previousX = 0;
    private int previousY = 0;
    private int nbShots = 0;
    private boolean convexHULL = true;
    long startTime;
    int sceneCounter = 0;
    @Getter
    protected int nbGoals = 0;
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
    private java.util.LinkedList<net.gazeplay.commons.utils.FixationPoint> fixationSequence;
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

    public void notifyNewRoundReady() {
        currentRoundStartTime = System.currentTimeMillis();
        takeScreenShot();
    }

    public void start() {
        config = Configuration.getInstance();
        if (config.isVideoRecordingEnabled()) {
            nameOfVideo = Utils.now() + "video";
            directoryOfVideo = getGameStatsOfTheDayDirectory().toString();
            VideoRecorder.start(nameOfVideo);
            VideoRecorderConfiguration.setVideoDirectory(getGameStatsOfTheDayDirectory());

        }
        lifeCycle.start(() -> {

            if (!config.isHeatMapDisabled())
                heatMap = instanciateHeatMapData(gameContextScene, heatMapPixelSize);
            startTime = System.currentTimeMillis();
            if (!config.isFixationSequenceDisabled())
                fixationSequence = new LinkedList<FixationPoint>();
            recordGazeMovements = e -> {
                int getX = (int) e.getX();
                int getY = (int) e.getY();
                if (!config.isHeatMapDisabled())
                    incHeatMap(getX, getY);
                if (!config.isFixationSequenceDisabled()) {
                    incFixationSequence(getX, getY);
                }
                if (config.isAreaOfInterestEnabled()) {
                    if (getX != previousX || getY != previousY && counter == 0) {
                        long timeToFixation = System.currentTimeMillis() - startTime;
                        previousX = getX;
                        previousY = getY;
                        long timeInterval = (timeToFixation - previousTime);
                        movementHistory.add(new CoordinatesTracker(getX, getY, timeToFixation, timeInterval,
                                System.currentTimeMillis()));
                        previousTime = timeToFixation;
                        counter++;
                        if (counter == 2)
                            counter = 0;
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
                    if (getX != previousX || getY != previousY && counter == 0) {
                        long timeElapsedMillis = System.currentTimeMillis() - startTime;
                        previousX = getX;
                        previousY = getY;
                        long timeInterval = (timeElapsedMillis - previousTime);
                        movementHistory.add(new CoordinatesTracker(getX, getY, timeElapsedMillis, timeInterval,
                                System.currentTimeMillis()));
                        previousTime = timeElapsedMillis;
                        counter++;
                        if (counter == 2)
                            counter = 0;
                    }
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
        if (config.isVideoRecordingEnabled()) {
            try {
                VideoRecorder.stop();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("Video couldn't stop");
            }
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

    public SavedStatsInfo saveStats() throws IOException {

        File todayDirectory = getGameStatsOfTheDayDirectory();
        final String heatmapFilePrefix = Utils.now() + "-heatmap";
        final String fixationSequenceFilePrefix = Utils.now() + "-fixationSequence";
        final String screenshotPrefix = Utils.now() + "-screenshot";

        File heatMapPngFile = new File(todayDirectory, heatmapFilePrefix + ".png");
        File heatMapCsvFile = new File(todayDirectory, heatmapFilePrefix + ".csv");

        File fixationSequencePngFile = new File(todayDirectory, fixationSequenceFilePrefix + ".png");

        File screenshotFile = new File(todayDirectory, screenshotPrefix + ".png");
        BufferedImage bImage = SwingFXUtils.fromFXImage(gameScreenShot, null);
        try {
            ImageIO.write(bImage, "png", screenshotFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SavedStatsInfo savedStatsInfo = new SavedStatsInfo(heatMapPngFile, heatMapCsvFile, screenshotFile,
                fixationSequencePngFile);

        this.savedStatsInfo = savedStatsInfo;
        if (this.heatMap != null) {
            saveHeatMapAsPng(heatMapPngFile);
            saveHeatMapAsCsv(heatMapCsvFile);
        }

        if (this.fixationSequence != null) {
            saveFixationSequenceAsPng(fixationSequencePngFile);
        }
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
        Runtime rt = Runtime.getRuntime();
        if (PlatformUtil.isWindows()) {
            try {
                Process proc = rt.exec("rename " + VideoRecorderConfiguration.getVideoDirectory() + "/"
                        + this.nameOfVideo + ".mov " + this.nameOfVideo + ".mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Process proc = rt.exec("mv " + VideoRecorderConfiguration.getVideoDirectory() + "/" + this.nameOfVideo
                        + ".mov " + VideoRecorderConfiguration.getVideoDirectory() + "/" + this.nameOfVideo + ".mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return VideoRecorderConfiguration.getVideoDirectory().toURI() + this.nameOfVideo + ".mp4";
    }

    File createInfoStatsFile() {
        File outputDirectory = getGameStatsOfTheDayDirectory();

        final String fileName = Utils.now() + "-info-game.csv";
        System.out.println("The output directory is " + outputDirectory.toURI());
        return new File(outputDirectory, fileName);
    }

    File getGameStatsOfTheDayDirectory() {
        File statsDirectory = new File(Utils.getUserStatsFolder(Configuration.getInstance().getUserName()));
        File gameDirectory = new File(statsDirectory, gameName);
        File todayDirectory = new File(gameDirectory, Utils.today());
        boolean outputDirectoryCreated = todayDirectory.mkdirs();
        log.info("outputDirectoryCreated = {}", outputDirectoryCreated);

        return todayDirectory;
    }

    void printLengthBetweenGoalsToString(PrintWriter out) {
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

    private void saveHeatMapAsPng(File outputPngFile) {

        log.info(String.format("Heatmap size: %3d X %3d", heatMap[0].length, heatMap.length));

        HeatMap map = new HeatMap(heatMap);

        try {
            map.saveToFile(outputPngFile);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void saveFixationSequenceAsPng(File outputPngFile) {

        // log.info(String.format("Fixation-Sequence size: %3d X %3d",
        // (int) (gameContextScene.getWidth() / heatMapPixelSize),
        // (int) (gameContextScene.getHeight() / heatMapPixelSize)));

        // FixationSequence sequence = new FixationSequence((int) (gameContextScene.getWidth() / heatMapPixelSize),
        // (int) (gameContextScene.getHeight() / heatMapPixelSize), fixationSequence);
        FixationSequence sequence = new FixationSequence((int) gameContextScene.getWidth(),
                (int) gameContextScene.getHeight(), fixationSequence);
        try {
            sequence.saveToFile(outputPngFile);
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

        if (fixationSequence.size() == 0) {
            previousGaze = 0;
        } else {
            previousGaze = (fixationSequence.get(fixationSequence.size() - 1)).getFirstGaze();
        }

        FixationPoint newGazePoint = new FixationPoint(System.currentTimeMillis(), 0, x, y);
        gazeDuration = Math.abs(previousGaze - newGazePoint.getFirstGaze());

        // if the new points coordinates are the same as last one's in the list then update the last fixationPoint in
        // the list
        // same coordinate points are a result of the eyetracker's frequency of sampling
        if (fixationSequence.size() > 1
                && (newGazePoint.getX() == fixationSequence.get(fixationSequence.size() - 1).getX())
                && (newGazePoint.getY() == fixationSequence.get(fixationSequence.size() - 1).getY())) {
            fixationSequence.get(fixationSequence.size() - 1).setGazeDuration(gazeDuration);
        } else { // else add the new point in the list
            newGazePoint.setGazeDuration(gazeDuration);
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

}
