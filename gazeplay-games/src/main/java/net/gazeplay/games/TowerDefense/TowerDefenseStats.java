package net.gazeplay.games.TowerDefense;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the Tower Defense game: one goal to reach for each enemy sent, one goal reached for each enemy
 * destroyed (since 3/11/2026; before, no goal was counted).
 */
public class TowerDefenseStats extends Stats {

    public TowerDefenseStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "TowerDefense";
    }

    public TowerDefenseStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "TowerDefense";
    }
}
