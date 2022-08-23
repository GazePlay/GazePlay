package net.gazeplay.games.pet;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.SelectionGamesStats;

import java.util.List;

public class PetStats extends SelectionGamesStats {

    public PetStats(Scene scene) {
        super(scene);
        this.gameName = "pet";
        setAccidentalShotPreventionPeriod(0);
    }

    public PetStats(Scene scene,
                    int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                    LifeCycle lifeCycle,
                    RoundsDurationReport roundsDurationReport,
                    List<List<FixationPoint>> fixationSequence,
                    List<CoordinatesTracker> movementHistory,
                    int[][] heatMap,
                    List<AreaOfInterest> aoiList,
                    SavedStatsInfo savedStatsInfo
    ) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle, roundsDurationReport,
            fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        this.gameName = "pet";
        setAccidentalShotPreventionPeriod(0);
    }
}
