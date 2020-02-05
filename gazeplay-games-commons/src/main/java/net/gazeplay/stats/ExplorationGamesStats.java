package net.gazeplay.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ExplorationGamesStats extends Stats {

    public ExplorationGamesStats(final Scene scene) {
        super(scene);
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
            out.print(Utils.todayCSV());
            out.print(',');
            out.print(Utils.time());
            out.print(',');
            out.print(computeTotalElapsedDuration());
            out.flush();
        }
        return null;
    }
}
