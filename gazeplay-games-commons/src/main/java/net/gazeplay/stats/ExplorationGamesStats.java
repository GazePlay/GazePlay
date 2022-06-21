package net.gazeplay.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.AreaOfInterest;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExplorationGamesStats extends Stats {

    public ExplorationGamesStats(final Scene scene) {
        super(scene);
    }

    public ExplorationGamesStats(final Scene scene,
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
    }

    public ExplorationGamesStats(final Scene scene, String name) {
        super(scene, name);
    }

    public ExplorationGamesStats(final Scene scene, String name,
                                 int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                 LifeCycle lifeCycle,
                                 RoundsDurationReport roundsDurationReport,
                                 List<List<FixationPoint>> fixationSequence,
                                 List<CoordinatesTracker> movementHistory,
                                 double[][] heatMap,
                                 List<AreaOfInterest> aoiList,
                                 SavedStatsInfo savedStatsInfo
    ) {
        super(scene, name, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle, roundsDurationReport,
            fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        super.saveStats();
        final File infoStatsFile = createInfoStatsFile();
        try (PrintWriter out = new PrintWriter(infoStatsFile, StandardCharsets.UTF_8)) {
            out.print("Date");
            out.print(',');
            out.print("Time");
            out.print(',');
            out.print("Total Time");
            out.print(',');
            out.println();
            out.print(DateUtils.todayCSV());
            out.print(',');
            out.print(DateUtils.timeNow());
            out.print(',');
            out.print(computeTotalElapsedDuration());
            out.flush();
        }
        return null;
    }
}
