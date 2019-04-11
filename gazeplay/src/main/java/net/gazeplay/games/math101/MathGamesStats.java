package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.SelectionGamesStats;

import java.io.IOException;

@Slf4j
public class MathGamesStats extends SelectionGamesStats {
    public MathGamesStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Math 101";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {

        SavedStatsInfo statsInfo = super.saveStats();

        log.debug("Stats saved");
        return statsInfo;
    }

}
