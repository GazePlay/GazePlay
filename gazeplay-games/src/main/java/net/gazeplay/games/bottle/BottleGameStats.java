package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class BottleGameStats extends Stats {

    public BottleGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "BottleGame";
    }

    public BottleGameStats(Scene gameContextScene,
                           int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                           LifeCycle lifeCycle,
                           RoundsDurationReport roundsDurationReport,
                           List<List<FixationPoint>> fixationSequence,
                           List<CoordinatesTracker> movementHistory,
                           double[][] heatMap,
                           List<AreaOfInterest> AOIList,
                           SavedStatsInfo savedStatsInfo
    ) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
        this.gameName = "BottleGame";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}
