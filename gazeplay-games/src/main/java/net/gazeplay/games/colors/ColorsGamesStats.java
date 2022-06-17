package net.gazeplay.games.colors;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.SelectionGamesStats;

import java.io.IOException;
import java.util.List;

/**
 * Stats for the color game.
 *
 * @author Thomas Medard
 */
@Slf4j
public class ColorsGamesStats extends SelectionGamesStats {

    public ColorsGamesStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Colors";
    }

    public ColorsGamesStats(Scene gameContextScene,
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
        this.gameName = "Colors";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        SavedStatsInfo statsInfo = super.saveStats();
        log.debug("Stats saved");
        return statsInfo;
    }
}
