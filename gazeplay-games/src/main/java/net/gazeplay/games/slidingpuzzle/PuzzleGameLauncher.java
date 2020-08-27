package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class PuzzleGameLauncher implements IGameLauncher<Stats, EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant>> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new SlidingPuzzleStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(
        final IGameContext gameContext,
        final EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        final Stats stats
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant);
    }

}
