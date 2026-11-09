package net.gazeplay.games.rushhour;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Embouteillage" (name code RushHour) and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). With a variant of before the review of 9/11/2026 (a number of level from 1 to 33): the levels
 * 1 to 11 are played easy, 12 to 22 normal, 23 to 33 hard, without end.
 */
public class RushHourGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new RushHourStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new RushHourStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new RushHour(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return new RushHour(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static RushHourLevel levelOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return RushHourLevel.of(grouped.getSubGroup());
        }
        if (gameVariant instanceof IntGameVariant old) {
            return old.getNumber() <= 11 ? RushHourLevel.EASY
                : old.getNumber() <= 22 ? RushHourLevel.NORMAL : RushHourLevel.HARD;
        }
        return RushHourLevel.NORMAL;
    }

    static RushHourEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? RushHourEnd.of(grouped.getValue())
            : RushHourEnd.ENDLESS;
    }
}
