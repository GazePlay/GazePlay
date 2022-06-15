package net.gazeplay.games.cups;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.games.cups.utils.CupsAndBallsStats;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class CupsBallsGameLauncher implements IGameLauncher<Stats, IntGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new CupsAndBallsStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  double[][] heatMap,
                                  List<AreaOfInterest> AOIList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new CupsAndBallsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant, Stats stats) {
        return new CupsAndBalls(gameContext, stats, gameVariant.getNumber(), 3);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntGameVariant gameVariant, Stats stats, double gameSeed) {
        return new CupsAndBalls(gameContext, stats, gameVariant.getNumber(), 3, gameSeed, "newGame");
    }
}
