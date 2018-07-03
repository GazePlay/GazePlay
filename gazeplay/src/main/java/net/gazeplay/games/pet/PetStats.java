package net.gazeplay.games.pet;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.SelectionGamesStats;

public class PetStats extends SelectionGamesStats {

    public PetStats(Scene scene) {
        super(scene);
        this.gameName = "pet";
        setAccidentalShotPreventionPeriod(0);
    }

}
