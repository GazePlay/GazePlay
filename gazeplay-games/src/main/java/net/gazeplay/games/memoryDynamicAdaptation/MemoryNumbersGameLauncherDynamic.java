package net.gazeplay.games.memoryDynamicAdaptation;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DynamicMemoryVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class MemoryNumbersGameLauncherDynamic implements IGameLauncher<Stats, DynamicMemoryVariant> {
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
                                       DynamicMemoryVariant gameVariant, Stats stats) {
        return new MemoryDynamic(MemoryDynamic.MemoryGameType.NUMBERS, gameContext, 2,
            2, stats, false);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    DynamicMemoryVariant gameVariant, Stats stats, double gameSeed) {
        return new MemoryDynamic(MemoryDynamic.MemoryGameType.NUMBERS, gameContext, 2,
            2, stats, false, gameSeed);
    }
}
