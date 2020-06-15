package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class BottleGameLauncher implements GameSpec.GameLauncher<BottleGameStats, GameSpec.DimensionGameVariant> {

    @Override
    public BottleGameStats createNewStats(Scene scene) {
        return new BottleGameStats(scene);
    }

    @Override
    public BottleGame createNewGame(IGameContext gameContext, GameSpec.DimensionGameVariant gameVariant, BottleGameStats stats) {
        BottleGame gameInstance;
        return new BottleGame(gameContext, stats);
    }
}
