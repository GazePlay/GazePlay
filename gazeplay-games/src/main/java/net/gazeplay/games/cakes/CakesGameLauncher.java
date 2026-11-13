package net.gazeplay.games.cakes;

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
 * Creates "La Fabrique à gâteaux" (name code Cakes) and its statistics (folder "Cakes", as before), for a new game or for
 * the replay of a saved one (see {@code IGameLauncher}). The variants of before the review (easy, normal, hard) are read
 * as copying the model at the same level, without end (maintainer's choice of 12/11/2026).
 */
public class CakesGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CakeStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new CakeStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static CakeFactory create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                              final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new CakeFactory(gameContext, stats, CakesMode.of(grouped.getGroup()),
                CakesLevel.of(grouped.getValue()), CakesEnd.of(grouped.getSubGroup()), gameSeed);
        }
        CakesLevel level = CakesLevel.NORMAL;
        if (gameVariant instanceof EnumGameVariant<?> former && former.getEnumValue() instanceof CakeGameVariant old) {
            level = switch (old) {
                case ONELAYER -> CakesLevel.EASY;
                case THREELAYERS -> CakesLevel.NORMAL;
                case THREELAYERSHIDEN -> CakesLevel.HARD;
            };
        }
        return new CakeFactory(gameContext, stats, CakesMode.COPY, level, CakesEnd.ENDLESS, gameSeed);
    }
}
