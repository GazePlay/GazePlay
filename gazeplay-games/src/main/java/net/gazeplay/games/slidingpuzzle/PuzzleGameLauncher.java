package net.gazeplay.games.slidingpuzzle;

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
 * Creates the sliding puzzle "Jeu de taquin" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). With a variant of before the review of 9/11/2026: the numbers or the pictures, the normal
 * level, without end.
 */
public class PuzzleGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new SlidingPuzzleStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SlidingPuzzleStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new SlidingPuzzle(gameContext, stats, contentOf(gameVariant), levelOf(gameVariant), endOf(gameVariant),
            -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return new SlidingPuzzle(gameContext, stats, contentOf(gameVariant), levelOf(gameVariant), endOf(gameVariant),
            gameSeed);
    }

    static PuzzleContent contentOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return PuzzleContent.of(grouped.getGroup());
        }
        if (gameVariant instanceof EnumGameVariant<?> old
            && old.getEnumValue() instanceof PuzzleGameVariantGenerator.PuzzleGameVariant value) {
            return value.getContent();
        }
        return PuzzleContent.PICTURES;
    }

    static PuzzleLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? PuzzleLevel.of(grouped.getSubGroup())
            : PuzzleLevel.NORMAL;
    }

    static PuzzleEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? PuzzleEnd.of(grouped.getValue())
            : PuzzleEnd.ENDLESS;
    }
}
