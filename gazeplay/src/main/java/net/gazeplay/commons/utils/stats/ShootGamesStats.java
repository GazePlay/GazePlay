package net.gazeplay.commons.utils.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.games.Utils;

import java.io.*;

public class ShootGamesStats extends Stats {

    public ShootGamesStats(Scene scene) {
        super(scene);
        setAccidentalShotPreventionPeriod(100);
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
            out.print("Nb Goals");
            out.print(',');
            out.print("Length");
            out.print(',');
            out.print("Average Length");
            out.print(',');
            out.print("Standard Déviation");
            out.print(',');
            out.print("Uncounted Shoots");
            out.print(',');
            for (int i = 0; i < getNbGoals(); i++) {
                out.print("shoot ");
                out.print(i);
                out.print(",");
            }
            out.println();

            out.print(Utils.todayCSV());
            out.print(',');
            out.print(Utils.time());
            out.print(',');
            out.print(computeTotalElapsedDuration());
            out.print(',');
            out.print(getNbGoals());
            out.print(',');
            out.print(getRoundsTotalAdditiveDuration());
            out.print(',');
            out.print(computeRoundsDurationAverageDuration());
            out.print(',');
            out.print(computeRoundsDurationStandardDeviation());
            out.print(',');
            out.print(getNbUnCountedShoots());
            out.print(',');
            printLengthBetweenGoalsToString(out);
            out.println();

            out.flush();
        }
        return null;
    }

}
