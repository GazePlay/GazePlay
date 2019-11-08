package net.gazeplay.games.race;

import javafx.scene.Scene;
import net.gazeplay.stats.ShootGamesStats;

class RaceGamesStats extends ShootGamesStats {

    RaceGamesStats(Scene scene, String gameType) {
        super(scene);
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }

}
