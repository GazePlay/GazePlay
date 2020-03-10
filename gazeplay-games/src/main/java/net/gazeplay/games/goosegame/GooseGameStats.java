package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

public class GooseGameStats extends Stats {


    public GooseGameStats(Scene gameContextScene, String gameName) {
        super(gameContextScene, gameName);
    }

    public void incrementNumberOfGoalsReached(int i) {
        nbGoalsReached += i;
        this.notifyNextRound();
    }
}
