package net.gazeplay.games.divisor;

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
 * Creates the game "Diviseur" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant (the games "Divisor" and "Rabbits" had none before the review of
 * 12/11/2026): the rabbits, burst at once, at the normal level (15 targets, as before).
 */
public class DivisorGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new DivisorStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new DivisorStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return game(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return game(gameContext, gameVariant, stats, gameSeed);
    }

    private static Divisor game(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                final double gameSeed) {
        return new Divisor(gameContext, stats, themeOf(gameVariant), touchOf(gameVariant), levelOf(gameVariant),
            gameSeed);
    }

    static DivisorTheme themeOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? DivisorTheme.of(grouped.getGroup())
            : DivisorTheme.RABBITS;
    }

    static DivisorTouch touchOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? DivisorTouch.of(grouped.getSubGroup())
            : DivisorTouch.AT_ONCE;
    }

    static DivisorLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? DivisorLevel.of(grouped.getValue())
            : DivisorLevel.NORMAL;
    }
}
