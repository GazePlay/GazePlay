package net.gazeplay.games.moles;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ShootGamesStats;

import java.util.List;

class MoleStats extends ShootGamesStats {

    MoleStats(Scene scene) {
        super(scene);
        this.gameName = "mole";
        setAccidentalShotPreventionPeriod(0);
    }

    MoleStats(Scene scene,
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
        this.gameName = "mole";
        setAccidentalShotPreventionPeriod(0);
    }
}
