package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class BibouleJumpStats extends Stats {

    public BibouleJumpStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "biboule-jump";
    }

    public BibouleJumpStats(Scene gameContextScene,
                            int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                            LifeCycle lifeCycle,
                            RoundsDurationReport roundsDurationReport,
                            List<List<FixationPoint>> fixationSequence,
                            List<CoordinatesTracker> movementHistory,
                            double[][] heatMap,
                            List<AreaOfInterest> aoiList,
                            SavedStatsInfo savedStatsInfo
    ) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        this.gameName = "biboule-jump";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached += increment;
    }
}
