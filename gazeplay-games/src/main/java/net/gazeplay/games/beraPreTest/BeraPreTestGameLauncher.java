package net.gazeplay.games.beraPreTest;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class BeraPreTestGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    private BeraPreTestGameStats gameStats;

    @Override
    public Stats createNewStats(Scene scene) {
        gameStats = new BeraPreTestGameStats(scene);
        return gameStats;
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  int[][] heatMap,
                                  List<AreaOfInterest> aoiList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        gameStats = new BeraPreTestGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
        return gameStats;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new BeraPreTest(gameContext, gameStats, gameContext.getTranslator(), false);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new BeraPreTest(gameContext, gameStats, gameContext.getTranslator(), true);
    }
}
