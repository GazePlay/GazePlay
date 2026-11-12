package net.gazeplay.games.creampie;

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
 * Creates "Tartes à la crème" (name code Creampie) and its statistics (folder "creampie", as before), for a new game or
 * for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class CreampieGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CreampieStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new CreampieStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    /**
     * @return the game of a variant; without a variant (an old shortcut), as before: the animals, medium targets,
     * thrown after a fixation, still, without end (maintainer's choice of 12/11/2026)
     */
    static CreamPie create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                           final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final java.util.List<String> extras = grouped.getExtras();
            return new CreamPie(gameContext, stats, CreamPiePictures.of(grouped.getGroup()),
                CreamPieSize.of(grouped.getValue()), CreamPieThrow.of(grouped.getSubGroup()),
                CreamPieMotion.of(extras.isEmpty() ? null : extras.get(0)),
                CreamPieEnd.of(extras.size() < 2 ? null : extras.get(1)), gameSeed);
        }
        return new CreamPie(gameContext, stats, CreamPiePictures.ANIMALS, CreamPieSize.MEDIUM, CreamPieThrow.FIXATION,
            CreamPieMotion.STILL, CreamPieEnd.ENDLESS, gameSeed);
    }
}
