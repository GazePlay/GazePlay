package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

public class BibouleJumpStats extends Stats {

    public BibouleJumpStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "biboule-jump";
    }

    public void incNbGoals(int increment) {
        nbGoals += increment;
    }
}
