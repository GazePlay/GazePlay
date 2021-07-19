package net.gazeplay.games.memory;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class MemoryNumbersGameLauncher implements IGameLauncher<Stats, DimensionDifficultyGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new MagicCardsGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new MagicCardsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionDifficultyGameVariant gameVariant, Stats stats) {
        return new Memory(Memory.MemoryGameType.NUMBERS, gameContext, gameVariant.getWidth(),
            gameVariant.getHeight(), gameVariant.getDifficulty(), stats, false);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                       DimensionDifficultyGameVariant gameVariant, Stats stats, double gameSeed) {
        return new Memory(Memory.MemoryGameType.NUMBERS, gameContext, gameVariant.getWidth(),
            gameVariant.getHeight(), gameVariant.getDifficulty(), stats, false, gameSeed);
    }
}
