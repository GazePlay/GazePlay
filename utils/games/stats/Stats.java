package utils.games.stats;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Stats {

    private final int heatMapPixelSize=5;
    private final int trail = 2;

    protected int nbGoals;
    protected long length;
    protected long beginTime;
    protected long zeroTime;
    protected ArrayList<Integer> lengthBetweenGoals;
    protected Scene scene;
    protected EventHandler<MouseEvent> recordMouseMovements;

    protected double[][] heatMap;



    public ArrayList<Integer> getLengthBetweenGoals() {
        return lengthBetweenGoals;
    }

    public Stats(Scene scene) {

        this.scene = scene;
        nbGoals = 0;
        beginTime = 0;
        length = 0;
        zeroTime = System.currentTimeMillis();
        lengthBetweenGoals = new ArrayList<Integer>(1000);
        recordMouseMovements = buildRecordMouseMovements();
        scene.addEventFilter(MouseEvent.ANY, recordMouseMovements);
        heatMap = new double[(int)scene.getHeight()/heatMapPixelSize][(int)scene.getWidth()/heatMapPixelSize];
    }

    private EventHandler<MouseEvent> buildRecordMouseMovements() {

        return new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e) {


                //in heatChart, x and y are opposed
                int x = ((int)e.getY()/heatMapPixelSize);
                int y = ((int)e.getX()/heatMapPixelSize);

              //  System.out.println(x + ", " + y);

                inc(x,y);

                for(int i = -trail; i<= trail; i++)
                    for(int j = -trail; j<= trail; j++){

                        inc(x+i,y+j);
                    }
                inc(x,y);
            }
        };
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
                ", zero time =" + getTotalTime() +
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

    public long getTotalTime() {

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

        scene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
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
}
