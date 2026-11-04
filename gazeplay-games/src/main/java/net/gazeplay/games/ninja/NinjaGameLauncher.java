package net.gazeplay.games.ninja;

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
import java.util.List;

/**
 * Creates the game "Ninja" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant (or with one of before the review of 4/11/2026): the fruits at random, at
 * the normal speed, cut at once, without end.
 */
public class NinjaGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new NinjaStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new NinjaStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return game(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return game(gameContext, gameVariant, stats, gameSeed);
    }

    private static Ninja game(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                              final double gameSeed) {
        return new Ninja(gameContext, stats, trajectoryOf(gameVariant), speedOf(gameVariant), cutOf(gameVariant),
            endOf(gameVariant), targetOf(gameVariant), gameSeed);
    }

    static NinjaTrajectory trajectoryOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? NinjaTrajectory.of(grouped.getValue())
            : NinjaTrajectory.RANDOM;
    }

    static NinjaSpeed speedOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? NinjaSpeed.of(grouped.getSubGroup())
            : NinjaSpeed.NORMAL;
    }

    static NinjaTarget targetOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? NinjaTarget.of(grouped.getGroup())
            : NinjaTarget.FRUITS;
    }

    static NinjaCut cutOf(final IGameVariant gameVariant) {
        return NinjaCut.of(extra(gameVariant, 0));
    }

    static NinjaEnd endOf(final IGameVariant gameVariant) {
        return NinjaEnd.of(extra(gameVariant, 1));
    }

    private static String extra(final IGameVariant gameVariant, final int index) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final List<String> extras = grouped.getExtras();
            return index < extras.size() ? extras.get(index) : null;
        }
        return null;
    }
}
