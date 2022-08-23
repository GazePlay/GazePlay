package net.gazeplay.games.beraV2;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class BeraV2GameStats extends HiddenItemsGamesStats {

    public BeraV2GameStats(Scene scene) {
        super(scene);
        this.gameName = "beraV2";
    }

    public BeraV2GameStats(Scene scene,
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
        this.gameName = "beraV2";
    }
}
