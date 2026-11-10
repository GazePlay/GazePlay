package net.gazeplay.games.connect4;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Quatre en ligne" (name code Connect4) and its statistics, for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). A variant of before the review of 10/11/2026 (see {@link Connect4GameVariant}) is
 * played against the easy computer, without end.
 */
public class Connect4GameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new Connect4Stats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Connect4Stats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence,
            lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new Connect4(gameContext, stats, opponentOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return new Connect4(gameContext, stats, opponentOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static Connect4Opponent opponentOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? Connect4Opponent.of(grouped.getSubGroup())
            : Connect4Opponent.EASY;
    }

    static Connect4End endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? Connect4End.of(grouped.getValue())
            : Connect4End.ENDLESS;
    }
}
