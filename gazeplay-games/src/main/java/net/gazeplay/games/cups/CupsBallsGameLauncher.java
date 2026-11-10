package net.gazeplay.games.cups;

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
import net.gazeplay.games.cups.utils.CupsAndBallsStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Gobelets Pass Pass" (name code CupsBalls) and its statistics, for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). A variant of before the review of 10/11/2026 (3 or 5 cups) is played easy (3 cups) or
 * hard (5 cups), without end (the former game had none).
 */
public class CupsBallsGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new CupsAndBallsStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new CupsAndBallsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence,
            lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new CupsAndBalls(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return new CupsAndBalls(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static CupsLevel levelOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return CupsLevel.of(grouped.getSubGroup());
        }
        if (gameVariant instanceof IntGameVariant old) {
            return old.getNumber() <= 3 ? CupsLevel.EASY : CupsLevel.HARD;
        }
        return CupsLevel.NORMAL;
    }

    static CupsEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? CupsEnd.of(grouped.getValue()) : CupsEnd.ENDLESS;
    }
}
