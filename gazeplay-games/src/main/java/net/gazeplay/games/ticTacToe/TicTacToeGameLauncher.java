package net.gazeplay.games.ticTacToe;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
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
 * Creates the Morpion (name code TicTacToe) and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review of 10/11/2026 is read as told in {@link TicTacToeGameVariant}.
 */
public class TicTacToeGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new TicTacToeStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new TicTacToeStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence,
            lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new TicTacToe(gameContext, stats, opponentOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return new TicTacToe(gameContext, stats, opponentOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static TicTacToeOpponent opponentOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return TicTacToeOpponent.of(grouped.getSubGroup());
        }
        if (gameVariant instanceof EnumGameVariant<?> old && old.getEnumValue() == TicTacToeGameVariant.P2) {
            return TicTacToeOpponent.DUO;
        }
        return TicTacToeOpponent.EASY;
    }

    static TicTacToeEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? TicTacToeEnd.of(grouped.getValue())
            : TicTacToeEnd.GAMES_1;
    }
}
