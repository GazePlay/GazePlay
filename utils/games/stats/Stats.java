package utils.games.stats;

import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import utils.games.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by schwab on 16/08/2017.
 */

public abstract class Stats {

    private final int heatMapPixelSize=5;
    private final int trail = 10;

    protected String gameName;

    protected int nbGoals;
    protected long length;
    protected long beginTime;
    protected long zeroTime;
    protected ArrayList<Integer> lengthBetweenGoals;
    protected javafx.scene.Scene scene;
    protected EventHandler<MouseEvent> recordMouseMovements;
    protected EventHandler<GazeEvent> recordGazeMovements;

    protected double[][] heatMap;

    public ArrayList<Integer> getLengthBetweenGoals() {
        return lengthBetweenGoals;
    }

    public void printLengthBetweenGoalsToString(PrintWriter out){

        for(Integer I : lengthBetweenGoals) {
            out.print(I.intValue());
            out.print(',');
        }
    }

    public Stats(Scene scene) {

        this.scene = scene;
        nbGoals = 0;
        beginTime = 0;
        length = 0;
        zeroTime = System.currentTimeMillis();
        lengthBetweenGoals = new ArrayList<Integer>(1000);

        System.out.println("GazeUtils ON : " + GazeUtils.isOn());

        if(GazeUtils.isOn()){

            recordGazeMovements = buildRecordGazeMovements();
            GazeUtils.addStats(this);
        }
        else {

            recordMouseMovements = buildRecordMouseMovements();
            scene.addEventFilter(MouseEvent.ANY, recordMouseMovements);
        }
        heatMap = new double[(int)scene.getHeight()/heatMapPixelSize][(int)scene.getWidth()/heatMapPixelSize];
    }

    protected void saveRawHeatMap(File file){

        PrintWriter out = null;

        try {
            out = new PrintWriter(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < heatMap.length; i++){

            for(int j = 0; j < heatMap[0].length-1; j++) {

                out.print((int)heatMap[i][j]);
                out.print(", ");
            }

            out.print((int)heatMap[i][heatMap[i].length-1]);
            out.println("");
        }
        out.flush();
    }

    public void savePNGHeatMap(File destination){

        Path HeatMapPath = Paths.get(Utils.getHeatMapPath());
        Path dest= Paths.get(destination.getAbsolutePath());

        try {
            Files.copy(HeatMapPath, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveStats(){

        File saveFile = new File(Utils.getStatsFolder());
        saveFile.mkdir();

        File gameFolder = new File(Utils.getStatsFolder()+gameName);
        gameFolder.mkdir();

        File savepath = new File(gameFolder.getAbsoluteFile() + Utils.FILESEPARATOR + Utils.today());
        savepath.mkdir();

        File heatMapCSVPath = new File(savepath.getAbsoluteFile() + Utils.FILESEPARATOR + Utils.now()+"-heatmap.csv");
        File heatMapPNGPath = new File(savepath.getAbsoluteFile() + Utils.FILESEPARATOR + Utils.now()+"-heatmap.png");

        saveRawHeatMap(heatMapCSVPath);
        savePNGHeatMap(heatMapPNGPath);
    }

    private EventHandler<GazeEvent> buildRecordGazeMovements(){

        return new EventHandler<GazeEvent>() {

            @Override
            public void handle(GazeEvent e) {

                incHeatMap((int) e.getX(), (int) e.getY());
            }
        };
    }

    private EventHandler<MouseEvent> buildRecordMouseMovements() {

        return new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e) {

                incHeatMap((int) e.getX(), (int) e.getY());
            }
        };
    }

    public void incHeatMap(int X, int Y){

        //in heatChart, x and y are opposed
        int x = Y/heatMapPixelSize;
        int y = X/heatMapPixelSize;

        for(int i = -trail; i<= trail; i++)
            for(int j = -trail; j<= trail; j++){

                if(Math.sqrt(i*i+j*j)<trail)
                    inc(x+i,y+j);
            }
    }

    private void inc(int x, int y){

        if(x>=0&&y>=0&&x<heatMap.length&&y<heatMap[0].length)
           // heatMap[heatMap[0].length - y][heatMap.length - x]++;
            heatMap[x][y]++;
    }

    public void start(){

        beginTime = System.currentTimeMillis();
    }

    public int getNbGoals() {

        return nbGoals;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "nbShoots=" + getNbGoals() +
                ", length=" + getLength() +
                ", average length=" + getAverageLength() +
                ", zero time =" + getTotalLength() +
                '}' + lengthBetweenGoals;
    }

    public long getLength() {

        return length;
    }

    public long getAverageLength(){

        if(nbGoals == 0)
            return 0;
        else
            return getLength()/ nbGoals;
    }

    public long getMedianLength(){

        if(nbGoals == 0)
            return 0;
        else {

            int nbElements = lengthBetweenGoals.size();

            ArrayList<Integer> sortedList = (ArrayList<Integer>)lengthBetweenGoals.clone();

            Collections.sort(sortedList);

            int middle = (int)(nbElements/2);

            if(nbElements%2==0){//number of elements is even, median is the average of the two central numbers

                middle-=1;
                return (sortedList.get(middle)+sortedList.get(middle+1))/2;

            }else{//number of elements is odd, median is the central number

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

        for(Integer I : lengthBetweenGoals){

            sum+=Math.pow((I.intValue()-average),2);
        }

        return sum/ nbGoals;
    }

    public double getSD() {

        return Math.sqrt(getVariance());
    }

    public double[][] getHeatMap() {
        return heatMap;
    }

    public void stop() {

        if(GazeUtils.isOn()){

            scene.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
        }
        else {

            scene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
        }
    }

    public void incNbGoals(){

        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((new Long(last)).intValue());
    }

    public ArrayList<Integer> getSortedLengthBetweenGoals(){

        int nbElements = lengthBetweenGoals.size();

        ArrayList<Integer> sortedList = (ArrayList<Integer>)lengthBetweenGoals.clone();

        Collections.sort(sortedList);

        ArrayList<Integer> normalList = (ArrayList<Integer>)lengthBetweenGoals.clone();

        int j = 0;

        for(int i = 0; i < nbElements ; i++) {

            if(i%2 == 0)
                normalList.set(j, sortedList.get(i));
            else {
                normalList.set(nbElements -1 - j, sortedList.get(i));
                j++;
            }
        }

        return normalList;
    }

    protected String getTodayFolder(){

        return Utils.getStatsFolder() + gameName + Utils.FILESEPARATOR + Utils.today() + Utils.FILESEPARATOR;
    }
}
