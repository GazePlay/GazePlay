package net.gazeplay.games.dottodot;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class DotToDotGameStats extends HiddenItemsGamesStats {

    public DotToDotGameStats(Scene scene) {
        super(scene);
        this.gameName = "dotToDot";
    }

    public DotToDotGameStats(Scene scene,
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
        gameName = "dotToDot";
    }
}
