package net.gazeplay.games.divisor;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ShootGamesStats;

import java.util.List;

/**
 * @author vincent
 */
public class DivisorStats extends ShootGamesStats {

    public DivisorStats(final Scene scene) {
        super(scene);
        this.gameName = "divisor";
        setAccidentalShotPreventionPeriod(0);
    }

    public DivisorStats(final Scene scene,
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
        this.gameName = "divisor";
        setAccidentalShotPreventionPeriod(0);
    }
}
