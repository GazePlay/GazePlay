package net.gazeplay.games.race;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class RaceGamesStats extends ShootGamesStats {

    public RaceGamesStats(Scene scene, String gameType) {
        super(scene);
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }

}
