package net.gazeplay.games.soundsoflife;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.SelectionGamesStats;

import java.io.IOException;

@Slf4j
public class SoundsOfLifeStats extends SelectionGamesStats {
    public SoundsOfLifeStats(Scene gameContextScene, String codeName) {
        super(gameContextScene);
        this.gameName = codeName;
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {

        SavedStatsInfo statsInfo = super.saveStats();

        log.debug("Stats saved");
        return statsInfo;
    }

}
