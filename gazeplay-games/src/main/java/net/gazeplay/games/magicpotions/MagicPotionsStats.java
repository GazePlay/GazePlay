package net.gazeplay.games.magicpotions;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.SelectionGamesStats;

import java.io.IOException;

@Slf4j
public class MagicPotionsStats extends SelectionGamesStats {

    MagicPotionsStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Magic Potions";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        SavedStatsInfo statsInfo = super.saveStats();
        log.debug("Stats saved");
        return statsInfo;
    }
}
