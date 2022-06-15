package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class OpinionsGameStats extends Stats {

    public OpinionsGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Opinions-game";
    }

    public OpinionsGameStats(Scene gameContextScene,
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
        this.gameName = "Opinions-game";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}
