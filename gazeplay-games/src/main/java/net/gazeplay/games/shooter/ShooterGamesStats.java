package net.gazeplay.games.shooter;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ShootGamesStats;

import java.util.List;

public class ShooterGamesStats extends ShootGamesStats {

    public ShooterGamesStats(Scene scene, String gameType) {
        super(scene);
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }

    public ShooterGamesStats(Scene scene, String gameType,
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
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }
}
