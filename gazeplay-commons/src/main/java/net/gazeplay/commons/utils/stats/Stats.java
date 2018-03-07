package net.gazeplay.commons.utils.stats;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.HeatMapUtils;
import net.gazeplay.commons.utils.games.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by schwab on 16/08/2017.
 */
@Slf4j
public class Stats implements GazeMotionListener {

    private final double heatMapPixelSize = computeHeatMapPixelSize();

    private final int trail = 10;

    protected String gameName;

    protected int nbGoals;

    protected long length;

    protected long beginTime;

    private long zeroTime;

    @Getter
    protected ArrayList<Integer> lengthBetweenGoals;

    private final EventHandler<MouseEvent> recordMouseMovements;
    private final EventHandler<GazeEvent> recordGazeMovements;

    private double[][] heatMap;

    private final Scene gameContextScene;

    public void printLengthBetweenGoalsToString(PrintWriter out) {

        for (Integer I : lengthBetweenGoals) {
            out.print(I.intValue());
            out.print(',');
        }
    }

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

    protected void saveRawHeatMap(File file) throws IOException {
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

    public void savePNGHeatMap(File destination) {

        Path HeatMapPath = Paths.get(HeatMapUtils.getHeatMapPath());
        Path dest = Paths.get(destination.getAbsolutePath());

        try {
            Files.copy(HeatMapPath, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Exception", e);
        }
    }

    public void saveStats() throws IOException {

        File saveFile = new File(Utils.getStatsFolder());
        saveFile.mkdir();

        File gameFolder = new File(Utils.getStatsFolder() + gameName);
        gameFolder.mkdir();

        File savepath = new File(gameFolder.getAbsoluteFile() + Utils.FILESEPARATOR + Utils.today());
        savepath.mkdir();

        File heatMapCSVPath = new File(savepath.getAbsoluteFile() + Utils.FILESEPARATOR + Utils.now() + "-heatmap.csv");
        File heatMapPNGPath = new File(savepath.getAbsoluteFile() + Utils.FILESEPARATOR + Utils.now() + "-heatmap.png");

        saveRawHeatMap(heatMapCSVPath);
        savePNGHeatMap(heatMapPNGPath);
    }

    @Override
    public void gazeMoved(javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incHeatMap(positionX, positionY);
    }

    public void incHeatMap(int X, int Y) {

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

    public void start() {

        beginTime = System.currentTimeMillis();
    }

    public int getNbGoals() {

        return nbGoals;
    }

    @Override
    public String toString() {
        return "Stats{" + "nbShoots = " + getNbGoals() + ", length = " + getLength() + ", average length = "
                + getAverageLength() + ", zero time = " + getTotalLength() + '}' + lengthBetweenGoals;
    }

    public long getLength() {

        return length;
    }

    public long getAverageLength() {

        if (nbGoals == 0)
            return 0;
        else
            return getLength() / nbGoals;
    }

    public long getMedianLength() {

        if (nbGoals == 0)
            return 0;
        else {

            int nbElements = lengthBetweenGoals.size();

            ArrayList<Integer> sortedList = (ArrayList<Integer>) lengthBetweenGoals.clone();

            Collections.sort(sortedList);

            int middle = (int) (nbElements / 2);

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

    public double getVariance() {

        double average = getAverageLength();

        double sum = 0;

        for (Integer I : lengthBetweenGoals) {

            sum += Math.pow((I.intValue() - average), 2);
        }

        return sum / nbGoals;
    }

    public double getSD() {

        return Math.sqrt(getVariance());
    }

    public double[][] getHeatMap() {

        return heatMap.clone();
    }

    public void stop() {
        gameContextScene.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
        gameContextScene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
    }

    public void incNbGoals() {
        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((int) last);
    }

    public ArrayList<Integer> getSortedLengthBetweenGoals() {

        int nbElements = lengthBetweenGoals.size();

        ArrayList<Integer> sortedList = (ArrayList<Integer>) lengthBetweenGoals.clone();

        Collections.sort(sortedList);

        ArrayList<Integer> normalList = (ArrayList<Integer>) lengthBetweenGoals.clone();

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

    protected String getTodayFolder() {

        return Utils.getStatsFolder() + gameName + Utils.FILESEPARATOR + Utils.today() + Utils.FILESEPARATOR;
    }
}
