package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class PuzzleGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant>> {
    
    @Override
    public Stats createNewStats(Scene scene) {
        return new slidingpuzzlestats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant,
        Stats stats
    ) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant);
    }

}
