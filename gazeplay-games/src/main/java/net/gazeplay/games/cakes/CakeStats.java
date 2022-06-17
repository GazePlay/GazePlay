package net.gazeplay.games.cakes;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class CakeStats extends Stats {

    public CakeStats(Scene scene) {
        super(scene);
        this.gameName = "Cakes";
    }

    public CakeStats(Scene scene,
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
        this.gameName = "Cakes";
    }
}
