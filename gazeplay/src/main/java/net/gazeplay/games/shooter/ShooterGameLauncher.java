package net.gazeplay.games.shooter;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;

public class ShooterGameLauncher implements GameSpec.GameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new ShooterGamesStats(scene, "biboule");
    }

    @Override
    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats) {
        return new Shooter(gameContext, stats, "biboule");
    }
}
