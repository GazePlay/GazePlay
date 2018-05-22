package net.gazeplay.games.moles;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class MoleStats extends ShootGamesStats {

    public MoleStats(Scene scene) {
        super(scene);
        this.gameName = "mole";
        setAccidentalShotPreventionPeriod(0);

    }
}
