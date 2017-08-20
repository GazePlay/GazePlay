package utils.games;

public class Stats {


    private int nbShoots;
    private long length;
    private long beginTime;
    private long zeroTime;

    public Stats() {

        nbShoots = 0;
        beginTime = 0;
        length = 0;
        zeroTime = System.currentTimeMillis();
    }

    public void incNbShoot(){

        nbShoots++;
        length += System.currentTimeMillis() - beginTime;
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
                '}';
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
