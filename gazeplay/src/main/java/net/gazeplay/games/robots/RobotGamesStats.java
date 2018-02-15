package net.gazeplay.games.robots;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class RobotGamesStats extends ShootGamesStats {

    public RobotGamesStats(Scene scene) {

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
