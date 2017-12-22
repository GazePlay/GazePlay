package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class BubblesGamesStats extends ShootGamesStats {

    public BubblesGamesStats(Scene scene) {

        super(scene);
        this.gameName = "bubbles";
    }

    public void incNbGoals() {

        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((new Long(last)).intValue());
    }
}
