package net.gazeplay.commons.utils.stats;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import org.tc33.jheatchart.HeatChart;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * Created by schwab on 16/08/2017.
 */
@Slf4j
public class Stats implements GazeMotionListener {

    private static final int trail = 10;

    private final double heatMapPixelSize = computeHeatMapPixelSize();

    private final EventHandler<MouseEvent> recordMouseMovements;
    private final EventHandler<GazeEvent> recordGazeMovements;
    private final Scene gameContextScene;
    protected String gameName;
    @Getter
    protected int nbGoals;
    @Getter
    protected long length;
    protected long beginTime;
    @Getter
    protected List<Integer> lengthBetweenGoals;
    private long zeroTime;
    private double[][] heatMap;

    @Getter
    private SavedStatsInfo savedStatsInfo;

    public Stats(Scene gameContextScene) {
        this(gameContextScene, null);
    }

    public Stats(Scene gameContextScene, String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;

        nbGoals = 0;
        beginTime = 0;
        length = 0;
        zeroTime = System.currentTimeMillis();
        lengthBetweenGoals = new ArrayList<Integer>(1000);

        recordGazeMovements = e -> incHeatMap((int) e.getX(), (int) e.getY());
        recordMouseMovements = e -> incHeatMap((int) e.getX(), (int) e.getY());

        gameContextScene.addEventFilter(GazeEvent.ANY, recordGazeMovements);
        gameContextScene.addEventFilter(MouseEvent.ANY, recordMouseMovements);

        int heatMapWidth = (int) (gameContextScene.getHeight() / heatMapPixelSize);
        int heatMapHeight = (int) (gameContextScene.getWidth() / heatMapPixelSize);
        log.info("heatMapWidth = {}, heatMapHeight = {}", heatMapWidth, heatMapHeight);
        heatMap = new double[heatMapWidth][heatMapHeight];
    }

    public void start() {
        beginTime = System.currentTimeMillis();
    }

    public void stop() {
        gameContextScene.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
        gameContextScene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
    }

    @Override
    public void gazeMoved(javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incHeatMap(positionX, positionY);
    }

    @Data
    public static class SavedStatsInfo extends Observable {
        private final File heatMapPngFile;
        private final File heatMapCsvFile;

        public void notifyFilesReady() {
            this.notifyObservers();
        }

    }

    public SavedStatsInfo saveStats() throws IOException {

        File todayDirectory = getGameStatsOfTheDayDirectory();
        final String heatmapFilePrefix = Utils.now() + "-heatmap";
        File heatMapPngFile = new File(todayDirectory, heatmapFilePrefix + ".png");
        File heatMapCsvFile = new File(todayDirectory, heatmapFilePrefix + ".csv");

        SavedStatsInfo savedStatsInfo = new SavedStatsInfo(heatMapPngFile, heatMapCsvFile);
        this.savedStatsInfo = savedStatsInfo;

        saveHeatMapAsPng(heatMapPngFile);
        saveHeatMapAsCsv(heatMapCsvFile);

        savedStatsInfo.notifyFilesReady();
        return savedStatsInfo;
    }

    public long computeAverageLength() {

        if (nbGoals == 0)
            return 0;
        else
            return getLength() / nbGoals;
    }

    public long computeMedianLength() {

        if (nbGoals == 0)
            return 0;
        else {

            int nbElements = lengthBetweenGoals.size();

            List<Integer> sortedList = new ArrayList<>(lengthBetweenGoals);
            Collections.sort(sortedList);

            int middle = nbElements / 2;

            if (nbElements % 2 == 0) {// number of elements is even, median is the average of the two central numbers

                middle -= 1;
                return (sortedList.get(middle) + sortedList.get(middle + 1)) / 2;

            } else {// number of elements is odd, median is the central number

                return sortedList.get(middle);
            }
        }
    }

    public long getTotalLength() {

        return System.currentTimeMillis() - zeroTime;
    }

    private double computeVariance() {

        double average = computeAverageLength();

        double sum = 0;

        for (Integer value : lengthBetweenGoals) {
            sum += Math.pow((value - average), 2);
        }

        return sum / nbGoals;
    }

    public double computeSD() {
        return Math.sqrt(computeVariance());
    }

    public double[][] getHeatMap() {
        return heatMap.clone();
    }

    public void incNbGoals() {
        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((int) last);
    }

    public List<Integer> getSortedLengthBetweenGoals() {

        int nbElements = lengthBetweenGoals.size();

        ArrayList<Integer> sortedList = new ArrayList<>(lengthBetweenGoals);
        Collections.sort(sortedList);

        ArrayList<Integer> normalList = new ArrayList<>(lengthBetweenGoals);

        int j = 0;

        for (int i = 0; i < nbElements; i++) {

            if (i % 2 == 0)
                normalList.set(j, sortedList.get(i));
            else {
                normalList.set(nbElements - 1 - j, sortedList.get(i));
                j++;
            }
        }

        return normalList;
    }

    @Override
    public String toString() {
        return "Stats{" + "nbShoots = " + getNbGoals() + ", length = " + getLength() + ", average length = "
                + computeAverageLength() + ", zero time = " + getTotalLength() + '}' + lengthBetweenGoals;
    }

    File createInfoStatsFile() {
        File outputDirectory = getGameStatsOfTheDayDirectory();
        final String fileName = Utils.now() + "-info-game.csv";
        return new File(outputDirectory, fileName);
    }

    File getGameStatsOfTheDayDirectory() {
        File statsDirectory = new File(Utils.getStatsFolder());
        File gameDirectory = new File(statsDirectory, gameName);
        File todayDirectory = new File(gameDirectory, Utils.today());

        boolean outputDirectoryCreated = todayDirectory.mkdirs();
        log.info("outputDirectoryCreated = {}", outputDirectoryCreated);

        return todayDirectory;
    }

    void printLengthBetweenGoalsToString(PrintWriter out) {

        for (Integer I : lengthBetweenGoals) {
            out.print(I.intValue());
            out.print(',');
        }
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

        // Step 1: Create our heat map chart using our data.
        HeatChart map = new HeatChart(heatMap);

        map.setHighValueColour(java.awt.Color.RED);
        map.setLowValueColour(java.awt.Color.lightGray);

        map.setShowXAxisValues(false);
        map.setShowYAxisValues(false);
        map.setChartMargin(0);

        try {
            map.saveToFile(outputPngFile);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void incHeatMap(int X, int Y) {

        // in heatChart, x and y are opposed
        int x = (int) (Y / heatMapPixelSize);
        int y = (int) (X / heatMapPixelSize);

        for (int i = -trail; i <= trail; i++)
            for (int j = -trail; j <= trail; j++) {

                if (Math.sqrt(i * i + j * j) < trail)
                    inc(x + i, y + j);
            }
    }

    private void inc(int x, int y) {

        if (x >= 0 && y >= 0 && x < heatMap.length && y < heatMap[0].length)
            // heatMap[heatMap[0].length - y][heatMap.length - x]++;
            heatMap[x][y]++;
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
}
