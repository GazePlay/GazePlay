package net.gazeplay.games.egg;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ShootGamesStats;

import java.util.List;

public class EggGameStats extends ShootGamesStats {

    public EggGameStats(Scene scene, String gameType) {
        super(scene);
        this.gameName = gameType;
    }

    public EggGameStats(Scene scene, String gameType,
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
        this.gameName = gameType;
    }
}
