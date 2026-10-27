package net.gazeplay.games.gazeplayEval;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Usual statistics of GazePlay for a session of an evaluation (statistics/GazePlayEval): the results of the
 * evaluation itself are written by {@link EvalResultsWriter}.
 */
public class GazeplayEvalGameStats extends HiddenItemsGamesStats {

    public GazeplayEvalGameStats(Scene scene) {
        super(scene);
        this.gameName = "GazePlayEval";
    }

    public GazeplayEvalGameStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "GazePlayEval";
    }
}
