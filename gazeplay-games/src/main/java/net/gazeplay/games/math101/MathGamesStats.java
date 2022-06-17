package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.SelectionGamesStats;

import java.io.IOException;
import java.util.List;

@Slf4j
public class MathGamesStats extends SelectionGamesStats {

    public MathGamesStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Math 101";
    }

    public MathGamesStats(Scene gameContextScene,
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
        this.gameName = "Math 101";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        SavedStatsInfo statsInfo = super.saveStats();
        log.debug("Stats saved");
        return statsInfo;
    }
}
