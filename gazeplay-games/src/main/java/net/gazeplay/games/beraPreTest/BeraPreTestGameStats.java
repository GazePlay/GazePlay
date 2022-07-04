package net.gazeplay.games.beraPreTest;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class BeraPreTestGameStats extends HiddenItemsGamesStats {

    public BeraPreTestGameStats(Scene scene) {
        super(scene);
        this.gameName = "beraPreTest";
    }

    public BeraPreTestGameStats(Scene scene,
                                int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                LifeCycle lifeCycle,
                                RoundsDurationReport roundsDurationReport,
                                List<List<FixationPoint>> fixationSequence,
                                List<CoordinatesTracker> movementHistory,
                                int[][] heatMap,
                                List<AreaOfInterest> aoiList,
                                SavedStatsInfo savedStatsInfo
    ) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle, roundsDurationReport,
            fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        this.gameName = "beraPreTest";
    }
}
