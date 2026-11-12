package net.gazeplay.games.dottodot;

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
 * Creates "Point par point" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review of 12/11/2026 (see {@link DotToDotGameVariant}): the numbers,
 * the normal level ("dynamic": progressive), without end; without a variant, the easy level.
 */
public class DotToDotGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new DotToDotGameStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new DotToDotGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static DotToDot create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                           final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new DotToDot(gameContext, stats, DotLevel.of(grouped.getValue()), DotLabels.of(grouped.getGroup()),
                DotEnd.of(grouped.getSubGroup()), gameSeed);
        }
        if (gameVariant instanceof EnumGameVariant<?> former && former.getEnumValue() instanceof DotToDotGameVariant old) {
            final DotLevel level = old.getLabel().contains("Dynamic") ? DotLevel.PROGRESSIVE : DotLevel.NORMAL;
            return new DotToDot(gameContext, stats, level, DotLabels.NUMBERS, DotEnd.ENDLESS, gameSeed);
        }
        return new DotToDot(gameContext, stats, DotLevel.EASY, DotLabels.NUMBERS, DotEnd.ENDLESS, gameSeed);
    }
}
