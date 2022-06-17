package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class WhereIsItStats extends HiddenItemsGamesStats {

    public WhereIsItStats(Scene scene, String gameName) {
        super(scene);
        this.gameName = gameName;
    }

    public WhereIsItStats(Scene scene, String gameName,
                          int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                          LifeCycle lifeCycle,
                          RoundsDurationReport roundsDurationReport,
                          List<List<FixationPoint>> fixationSequence,
                          List<CoordinatesTracker> movementHistory,
                          double[][] heatMap,
                          List<AreaOfInterest> aoiList,
                          SavedStatsInfo savedStatsInfo
    ) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle, roundsDurationReport,
            fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        this.gameName = gameName;
    }
}
