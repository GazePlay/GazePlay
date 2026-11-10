package net.gazeplay.games.connect4;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.SelectionGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the game "Connect4" (see {@code SelectionGamesStats}): one goal per game, reached when the player wins
 * it (with two players, when it ends).
 */
public class Connect4Stats extends SelectionGamesStats {

    public Connect4Stats(Scene scene) {
        super(scene);
        this.gameName = "connect4";
    }

    public Connect4Stats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                         ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                         RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle,
            roundsDurationReport, savedStatsInfo);
        this.gameName = "connect4";
    }
}
