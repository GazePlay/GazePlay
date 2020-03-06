package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.stats.ShootGamesStats;

public class SpotTheDifferencesStats extends ShootGamesStats {

    public SpotTheDifferencesStats(Scene scene) {
        super(scene);
        this.gameName = "spotthedifferences";
    }

    public void incNbGoals(int incr){
        nbGoals+=incr;
    }
}
