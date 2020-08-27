package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;

public class BottleGameLauncher implements IGameLauncher<BottleGameStats, IntGameVariant> {

    @Override
    public BottleGameStats createNewStats(Scene scene) {
        return new BottleGameStats(scene);
    }

    @Override
    public BottleGame createNewGame(IGameContext gameContext, IntGameVariant gameVariant, BottleGameStats stats) {
        return new BottleGame(gameContext, stats, gameVariant.getNumber());
    }
}
