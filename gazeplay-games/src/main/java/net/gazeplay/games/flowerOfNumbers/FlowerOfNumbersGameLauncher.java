package net.gazeplay.games.flowerOfNumbers;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.AreaOfInterest;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.List;

public class FlowerOfNumbersGameLauncher  implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "FlowerOfNumbers");
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUncountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  int[][] heatMap,
                                  List<AreaOfInterest> aoiList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new Stats(scene, "FlowerOfNumbers", nbGoalsReached, nbGoalsToReach, nbUncountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new FlowerOfNumbersGame(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new FlowerOfNumbersGame(gameContext, stats, gameSeed);
    }
}
