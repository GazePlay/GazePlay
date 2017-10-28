package net.gazeplay.games.ninja;

import javafx.scene.Scene;
import net.gazeplay.utils.stats.ShootGamesStats;

public class NinjaStats extends ShootGamesStats {

    public NinjaStats(Scene scene) {
        super(scene);
        this.gameName = "ninja";
    }

    @Override
    public void saveStats() {
        super.saveStats();
    }

}
