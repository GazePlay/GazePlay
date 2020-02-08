package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class PuzzleGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant>> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new SlidingPuzzleStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(
        final IGameContext gameContext,
        final GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        final Stats stats
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant);
    }

}
