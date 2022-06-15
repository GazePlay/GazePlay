package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

import java.util.List;

public class WhereIsTheAnimalGameLauncher implements IGameLauncher<Stats, DimensionDifficultyGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.ANIMAL_NAME.getGameName());
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
        return new WhereIsItStats(scene, WhereIsItGameType.ANIMAL_NAME.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
                lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(
                gameVariant.getDifficulty().equals("DYNAMIC") ? WhereIsItGameType.ANIMAL_NAME_DYNAMIC : WhereIsItGameType.ANIMAL_NAME,
                gameVariant.getWidth(), gameVariant.getHeight(), false, gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats, double gameSeed) {
        return new WhereIsIt(WhereIsItGameType.ANIMAL_NAME, gameVariant.getWidth(), gameVariant.getHeight(), false, gameContext, stats, gameSeed);
    }
}
