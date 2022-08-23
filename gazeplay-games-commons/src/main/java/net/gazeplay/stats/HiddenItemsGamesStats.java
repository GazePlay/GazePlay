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

public class HiddenItemsGamesStats extends Stats {

    public HiddenItemsGamesStats(final Scene scene) {
        super(scene);
    }

    public HiddenItemsGamesStats(final Scene scene,
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
            out.print("Nb Goals");
            out.print(',');
            out.print("Length");
            out.print(',');
            out.print("Average Length");
            out.print(',');
            out.print("Standard Deviation");
            out.print(',');
            for (int i = 0; i < getNbGoalsToReach(); i++) {
                out.print("shot ");
                out.print(i);
                out.print(",");
            }
            out.println();

            out.print(DateUtils.todayCSV());
            out.print(',');
            out.print(DateUtils.timeNow());
            out.print(',');
            out.print(computeTotalElapsedDuration());
            out.print(',');
            out.print(getNbGoalsToReach());
            out.print(',');
            out.print(getRoundsTotalAdditiveDuration());
            out.print(',');
            out.print(computeRoundsDurationAverageDuration());
            out.print(',');
            out.print(computeRoundsDurationStandardDeviation());
            out.print(',');
            printLengthBetweenGoalsToString(out);
            out.println();

            out.flush();
        }
        return null;
    }

}
