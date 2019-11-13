/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.stats.SelectionGamesStats;

@Slf4j
class SlidingPuzzleStats extends SelectionGamesStats {

    SlidingPuzzleStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Sliding Puzzle";
    }

}
