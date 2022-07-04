package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class GooseGameStats extends Stats {

    public GooseGameStats(Scene gameContextScene, String gameName) {
        super(gameContextScene, gameName);
    }

    public GooseGameStats(Scene gameContextScene, String gameName,
                          int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                          LifeCycle lifeCycle,
                          RoundsDurationReport roundsDurationReport,
                          List<List<FixationPoint>> fixationSequence,
                          List<CoordinatesTracker> movementHistory,
                          int[][] heatMap,
                          List<AreaOfInterest> aoiList,
                          SavedStatsInfo savedStatsInfo
    ) {
        super(gameContextScene, gameName, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    public void incrementNumberOfGoalsReached(int i) {
        nbGoalsReached += i;
        this.notifyNextRound();
    }
}
