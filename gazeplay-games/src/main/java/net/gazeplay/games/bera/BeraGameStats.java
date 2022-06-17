package net.gazeplay.games.bera;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class BeraGameStats extends HiddenItemsGamesStats {

    public BeraGameStats(Scene scene) {
        super(scene);
        this.gameName = "bera";
    }

    public BeraGameStats(Scene scene,
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
        this.gameName = "bera";
    }
}
