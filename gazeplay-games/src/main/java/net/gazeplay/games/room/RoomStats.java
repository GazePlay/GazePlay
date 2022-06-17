package net.gazeplay.games.room;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.ExplorationGamesStats;

import java.util.List;

@Slf4j
public class RoomStats extends ExplorationGamesStats {

    public RoomStats(Scene scene) {
        super(scene);
        this.gameName = "room";
    }

    public RoomStats(Scene scene,
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
        this.gameName = "room";
    }
}
