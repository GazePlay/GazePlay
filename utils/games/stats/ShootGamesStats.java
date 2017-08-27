package utils.games.stats;

import javafx.scene.Scene;
import utils.games.Utils;

import java.io.File;

public class ShootGamesStats extends Stats{

    protected int nbUnCountedShoots;

    public ShootGamesStats(Scene scene) {

        super(scene);

        nbUnCountedShoots = 0;
    }

    public void incNbGoals(){

        long last = System.currentTimeMillis() - beginTime;
        if(last>100) {
            nbGoals++;
            length += last;
            lengthBetweenGoals.add((new Long(last)).intValue());
        }else{

            nbUnCountedShoots++;
        }
    }

    public int getNbUnCountedShoots() {
        return nbUnCountedShoots;
    }

    @Override
    public void saveStats(){

        super.saveStats();
    }


}
