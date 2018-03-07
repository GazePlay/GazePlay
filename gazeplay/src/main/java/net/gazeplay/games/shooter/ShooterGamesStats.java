package net.gazeplay.games.shooter;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class ShooterGamesStats extends ShootGamesStats {

    public ShooterGamesStats(Scene scene, String gameType) {

        super(scene);
        this.gameName = gameType;
    }

    public void incNbGoals() {

        long last = System.currentTimeMillis() - beginTime;
        nbGoals++;
        length += last;
        lengthBetweenGoals.add((new Long(last)).intValue());
    }
}
