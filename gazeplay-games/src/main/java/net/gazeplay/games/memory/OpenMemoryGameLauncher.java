package net.gazeplay.games.memory;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class OpenMemoryGameLauncher implements IGameLauncher<Stats, DimensionDifficultyGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new MagicCardsGamesStats(scene);
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
        return new MagicCardsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats) {
        return new Memory(Memory.MemoryGameType.DEFAULT, gameContext, gameVariant.getWidth(),
            gameVariant.getHeight(), gameVariant.getDifficulty(), stats, true);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats, double gameSeed) {
        return new Memory(Memory.MemoryGameType.DEFAULT, gameContext, gameVariant.getWidth(),
            gameVariant.getHeight(), gameVariant.getDifficulty(), stats, true, gameSeed);
    }
}
