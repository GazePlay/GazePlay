/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import java.io.IOException;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.SelectionGamesStats;

/**
 *
 * @author Peter Bardawil
 */
@Slf4j
public class slidingpuzzlestats extends SelectionGamesStats {

    public slidingpuzzlestats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Sliding Puzzle";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {

        SavedStatsInfo statsInfo = super.saveStats();

        log.debug("Stats saved");
        return statsInfo;
    }
}
