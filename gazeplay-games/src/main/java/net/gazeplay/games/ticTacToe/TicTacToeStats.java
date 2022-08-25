package net.gazeplay.games.ticTacToe;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.ShootGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

class TicTacToeStats extends ShootGamesStats {

    TicTacToeStats(Scene scene) {
        super(scene);
        this.gameName = "noughts and crosses";
        setAccidentalShotPreventionPeriod(0);
    }

    TicTacToeStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "noughts and crosses";
        setAccidentalShotPreventionPeriod(0);
    }

}
