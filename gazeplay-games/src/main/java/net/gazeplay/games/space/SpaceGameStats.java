package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class SpaceGameStats extends Stats {

    public SpaceGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "space-game";
    }

    public SpaceGameStats(Scene gameContextScene,
                          int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                          LifeCycle lifeCycle,
                          RoundsDurationReport roundsDurationReport,
                          List<List<FixationPoint>> fixationSequence,
                          List<CoordinatesTracker> movementHistory,
                          int[][] heatMap,
                          List<AreaOfInterest> aoiList,
                          SavedStatsInfo savedStatsInfo
    ) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        this.gameName = "space-game";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}
