package net.gazeplay.games.rushhour;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.SelectionGamesStats;

import java.util.List;

public class RushHourStats extends SelectionGamesStats {

    public RushHourStats(Scene scene) {
        super(scene);
        this.gameName = "rushHour";
        setAccidentalShotPreventionPeriod(0);
    }

    public RushHourStats(Scene scene,
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
        this.gameName = "rushHour";
        setAccidentalShotPreventionPeriod(0);
    }
}
