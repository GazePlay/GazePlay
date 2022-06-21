package net.gazeplay.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.AreaOfInterest;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.List;

public class SelectionGamesStats extends Stats {

    public SelectionGamesStats(final Scene gameContextScene) {
        super(gameContextScene);
    }

    public SelectionGamesStats(final Scene gameContextScene,
                               int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                               LifeCycle lifeCycle,
                               RoundsDurationReport roundsDurationReport,
                               List<List<FixationPoint>> fixationSequence,
                               List<CoordinatesTracker> movementHistory,
                               double[][] heatMap,
                               List<AreaOfInterest> aoiList,
                               SavedStatsInfo savedStatsInfo
    ) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }
}
