package net.gazeplay.games.order;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ShootGamesStats;

import java.util.List;

/**
 * @author Vincent
 */
public class OrderStats extends ShootGamesStats {

    public OrderStats(final Scene scene) {
        super(scene);
        this.gameName = "order";
    }

    public OrderStats(final Scene scene,
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
        this.gameName = "order";
    }
}
