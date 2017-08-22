package utils.games;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class Stats {

    private final int heatMapPixelSize=20;

    private int nbShoots;
    private long length;
    private long beginTime;
    private long zeroTime;
    private ArrayList<Integer> shoots;
    private Scene scene;
    private EventHandler<MouseEvent> recordMouseMovements;

    private double[][] heatMap;

    private int nbUnCountedShoots;

    public ArrayList<Integer> getShoots() {
        return shoots;
    }



    public Stats(Scene scene) {

        this.scene = scene;
        nbShoots = 0;
        beginTime = 0;
        length = 0;
        nbUnCountedShoots = 0;
        zeroTime = System.currentTimeMillis();
        shoots = new ArrayList<Integer>(1000);
        recordMouseMovements = buildRecordMouseMovements();
        scene.addEventFilter(MouseEvent.ANY, recordMouseMovements);
        heatMap = new double[(int)scene.getWidth()/heatMapPixelSize+1][(int)scene.getHeight()/heatMapPixelSize+1];
    }

    private EventHandler<MouseEvent> buildRecordMouseMovements() {

        return new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e) {

                int x = ((int)e.getX()/heatMapPixelSize);
                int y = ((int)e.getY()/heatMapPixelSize);

                System.out.println(x + ", " + y);

                if(x>=0&&y>=0)
                    heatMap[x][y]++;
            }
        };
    }

    public void incNbShoot(){


        long last = System.currentTimeMillis() - beginTime;
        if(last>100) {
            nbShoots++;
            length += last;
            shoots.add((new Long(last)).intValue());
        }else{

            nbUnCountedShoots++;
        }
    }

    public void start(){

        beginTime = System.currentTimeMillis();
    }

    public int getNbshoots() {

        return nbShoots;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "nbShoots=" + getNbshoots() +
                ", length=" + getLength() +
                ", average length=" + getAverageLength() +
                ", zero time =" + getTotalTime() +
                '}' + shoots;
    }

    public long getLength() {

        return length;
    }

    public long getAverageLength(){

        if(nbShoots == 0)
            return 0;
        else
            return getLength()/nbShoots;
    }

    public long getTotalTime() {

        return System.currentTimeMillis() - zeroTime;
    }

    public double getVariance() {

        double average = getAverageLength();

        double sum = 0;

        for(Integer I : shoots){

            sum+=Math.pow((I.intValue()-average),2);
        }

        return sum/nbShoots;
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

    public int getNbUnCountedShoots() {
        return nbUnCountedShoots;
    }
}
