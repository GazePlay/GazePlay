package net.gazeplay.games.blocs;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class BlocsGamesStats extends HiddenItemsGamesStats {

    public BlocsGamesStats(Scene scene) {
        super(scene);
        gameName = "blocs";
    }

    public BlocsGamesStats(Scene scene,
                           int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                           LifeCycle lifeCycle,
                           RoundsDurationReport roundsDurationReport,
                           List<List<FixationPoint>> fixationSequence,
                           List<CoordinatesTracker> movementHistory,
                           double[][] heatMap,
                           List<AreaOfInterest> AOIList,
                           SavedStatsInfo savedStatsInfo
    ) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle, roundsDurationReport,
            fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
        gameName = "blocs";
    }
}
