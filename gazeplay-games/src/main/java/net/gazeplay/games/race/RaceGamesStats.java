package net.gazeplay.games.race;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ShootGamesStats;

import java.util.List;

class RaceGamesStats extends ShootGamesStats {

    RaceGamesStats(Scene scene, String gameType) {
        super(scene);
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }

    RaceGamesStats(Scene scene, String gameType,
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
        setAccidentalShotPreventionPeriod(0);
    }
}
