package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

public class BottleGameStats extends Stats {

    public BottleGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "BottleGame";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}
