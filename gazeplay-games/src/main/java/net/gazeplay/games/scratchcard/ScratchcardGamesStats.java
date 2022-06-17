package net.gazeplay.games.scratchcard;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.List;

public class ScratchcardGamesStats extends HiddenItemsGamesStats {

    public ScratchcardGamesStats(Scene scene) {
        super(scene);
        gameName = "Scratchcard";
    }

    public ScratchcardGamesStats(Scene scene,
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
        gameName = "Scratchcard";
    }
}
