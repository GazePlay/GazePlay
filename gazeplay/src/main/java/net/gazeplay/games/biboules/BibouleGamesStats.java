package net.gazeplay.games.biboules;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class BibouleGamesStats extends ShootGamesStats {

    public BibouleGamesStats(Scene scene) {

        super(scene);
        this.gameName = "biboules";
    }

    public void incNbGoals() {

        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((new Long(last)).intValue());
    }
}
