/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.stats.SelectionGamesStats;

import java.util.List;

@Slf4j
class SlidingPuzzleStats extends SelectionGamesStats {

    SlidingPuzzleStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Sliding Puzzle";
    }

    SlidingPuzzleStats(Scene gameContextScene,
                       int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                       LifeCycle lifeCycle,
                       RoundsDurationReport roundsDurationReport,
                       List<List<FixationPoint>> fixationSequence,
                       List<CoordinatesTracker> movementHistory,
                       double[][] heatMap,
                       List<AreaOfInterest> AOIList,
                       SavedStatsInfo savedStatsInfo
    ) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
        this.gameName = "Sliding Puzzle";
    }
}
