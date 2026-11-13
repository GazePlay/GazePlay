package net.gazeplay.games.blocs;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
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
 * Creates "Blocs" (name code Blocks) and its statistics (as before), for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). The variants of before the review (a grid of 2 × 2 to 9 × 9) are read at the nearest
 * level, the blocks vanishing at the gaze, with the animals, without end.
 */
public class BlocsGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new BlocsGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BlocsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Blocs create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                        final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final String pictures = grouped.getExtras().isEmpty() ? null : grouped.getExtras().get(0);
            return new Blocs(gameContext, stats, BlocsMode.of(grouped.getGroup()), BlocsLevel.of(grouped.getValue()),
                BlocsEnd.of(grouped.getSubGroup()), BlocsPictures.of(pictures), gameSeed);
        }
        final BlocsLevel level = gameVariant instanceof DimensionGameVariant former
            ? BlocsLevel.of(Math.max(former.getWidth(), former.getHeight())) : BlocsLevel.NORMAL;
        return new Blocs(gameContext, stats, BlocsMode.GAZE, level, BlocsEnd.ENDLESS, BlocsPictures.ANIMALS, gameSeed);
    }
}
