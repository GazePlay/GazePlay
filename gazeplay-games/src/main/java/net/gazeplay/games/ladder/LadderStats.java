package net.gazeplay.games.ladder;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class LadderStats extends Stats {

    public LadderStats(Scene scene) {
        super(scene);
        this.gameName = "Ladder";
    }

    public LadderStats(Scene scene,
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
        this.gameName = "Ladder";
    }
}
