package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;

public class PuzzleGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant>> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new SlidingPuzzleStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SlidingPuzzleStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(
        final IGameContext gameContext,
        final GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        final Stats stats
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant);
    }

    @Override
    public GameLifeCycle replayGame(
        final IGameContext gameContext,
        final GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        final Stats stats, double gameSeed
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant, gameSeed);
    }

}
