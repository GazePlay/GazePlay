package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

public class OpinionsGameStats extends Stats {

    public OpinionsGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Opinions-game";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}
