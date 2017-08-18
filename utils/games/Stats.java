package utils.games;

public class Stats {


    private int nbShoots;
    private long length;
    private long beginTime;
   // private long endTime;

    public Stats() {

        nbShoots = 0;
        beginTime = 0;
        length = 0;
    }

    public void incNbShoot(){

        nbShoots++;
        length += System.currentTimeMillis() - beginTime;
    }

    public void newBeginTime(){

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
                ", average length=" + averageLength() +

                '}';
    }

    public long getLength() {

        return length;
    }

    public long averageLength(){

        if(nbShoots == 0)
            return 0;
        else
            return getLength()/nbShoots;
    }
}
