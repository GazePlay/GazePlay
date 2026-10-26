package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "Puzzle" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class PuzzleGameLauncher implements IGameLauncher<Stats, EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant>> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new SlidingPuzzleStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SlidingPuzzleStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(
        final IGameContext gameContext,
        final EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        final Stats stats
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant);
    }

    @Override
    public GameLifeCycle replayGame(
        final IGameContext gameContext,
        final EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        final Stats stats, double gameSeed
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant, gameSeed);
    }

}
