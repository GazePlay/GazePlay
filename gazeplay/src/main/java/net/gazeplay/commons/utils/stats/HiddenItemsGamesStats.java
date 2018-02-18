package net.gazeplay.commons.utils.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.games.Utils;

import java.io.*;

public class HiddenItemsGamesStats extends Stats {

    public HiddenItemsGamesStats(Scene scene) {

        super(scene);
    }

    @Override
    public void saveStats() throws IOException {
        super.saveStats();

        final File infoStatsFile = Utils.createInfoStatsFile(getTodayFolder());
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
            for (int i = 0; i < getLengthBetweenGoals().size(); i++) {
                out.print("shoot ");
                out.print(i);
                out.print(",");
            }
            out.println();

            out.print(Utils.todayCSV());
            out.print(',');
            out.print(Utils.time());
            out.print(',');
            out.print(getTotalLength());
            out.print(',');
            out.print(getNbGoals());
            out.print(',');
            out.print(getLength());
            out.print(',');
            out.print(getAverageLength());
            out.print(',');
            out.print(getSD());
            out.print(',');
            printLengthBetweenGoalsToString(out);
            out.println();

            out.flush();
        }
    }

}
