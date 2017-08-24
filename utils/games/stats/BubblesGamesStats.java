package utils.games.stats;

import javafx.scene.Scene;

public class BubblesGamesStats extends Stats{

    public BubblesGamesStats(Scene scene) {

        super(scene);
    }

    public void incNbShoot(){

        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((new Long(last)).intValue());
    }
}
