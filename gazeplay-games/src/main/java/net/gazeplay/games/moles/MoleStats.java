package net.gazeplay.games.moles;

import javafx.scene.Scene;
import net.gazeplay.stats.ShootGamesStats;

class MoleStats extends ShootGamesStats {

    MoleStats(Scene scene) {
        super(scene);
        this.gameName = "mole";
        setAccidentalShotPreventionPeriod(0);
    }

}
