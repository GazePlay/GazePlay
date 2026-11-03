package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the game "SpaceGame" (see {@code Stats}), named as before ("space-game"): one goal to reach for each
 * alien come and for the boss, one goal reached for each of them destroyed (since 3/11/2026; before, the goals
 * reached were the score, set at once).
 */
public class SpaceGameStats extends Stats {

    public SpaceGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "space-game";
    }

    public SpaceGameStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "space-game";
    }
}
