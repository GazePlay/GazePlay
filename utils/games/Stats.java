package utils.games;

import java.util.ArrayList;

public class Stats {


    private int nbShoots;
    private long length;
    private long beginTime;
    private long zeroTime;
    private ArrayList<Integer> shoots;

    public ArrayList<Integer> getShoots() {
        return shoots;
    }



    public Stats() {

        nbShoots = 0;
        beginTime = 0;
        length = 0;
        zeroTime = System.currentTimeMillis();
        shoots = new ArrayList<Integer>(1000);
    }

    public void incNbShoot(){

        nbShoots++;
        long last = System.currentTimeMillis() - beginTime;
        length += last;
        shoots.add((new Long (last)).intValue());
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
}
