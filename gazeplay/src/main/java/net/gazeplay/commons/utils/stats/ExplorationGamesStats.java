package net.gazeplay.commons.utils.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.games.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ExplorationGamesStats extends Stats {

    public ExplorationGamesStats(Scene scene) {
        super(scene);
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        super.saveStats();
        final File infoStatsFile = createInfoStatsFile();
        try (PrintWriter out = new PrintWriter(infoStatsFile, "UTF-8")) {
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
