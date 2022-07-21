package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.AreaOfInterest;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

import java.util.List;


public class WhereIsTheShapeGameLauncher implements IGameLauncher<Stats, DimensionDifficultyGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.SHAPES.getGameName());
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
        return new WhereIsItStats(scene, WhereIsItGameType.SHAPES.getGameName(), nbGoalsReached, nbGoalsToReach, nbUncountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(
            gameVariant.getDifficulty().equals("easy") ? WhereIsItGameType.SHAPES_EASY : WhereIsItGameType.SHAPES,
            gameVariant.getWidth(), gameVariant.getHeight(), false, gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats, double gameSeed) {
        return new WhereIsIt(
            gameVariant.getDifficulty().equals("easy") ? WhereIsItGameType.SHAPES_EASY : WhereIsItGameType.SHAPES,
            gameVariant.getWidth(), gameVariant.getHeight(), false, gameContext, stats, gameSeed);
    }
}
