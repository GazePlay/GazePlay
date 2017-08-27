package utils.games.stats;

import javafx.scene.Scene;

public class BubblesGamesStats extends Stats{

    public BubblesGamesStats(Scene scene) {

        super(scene);
        this.gameName = "bubbles";
    }

    public void incNbGoals(){

        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((new Long(last)).intValue());
    }

    @Override
    public void saveStats() {
        super.saveStats();
    }

}
