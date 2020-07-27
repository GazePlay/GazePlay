package net.gazeplay.games.paperScissorsStone;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

public class PaperScissorsStoneStats extends Stats {

    public PaperScissorsStoneStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Paper-Scissors-Stone";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}
