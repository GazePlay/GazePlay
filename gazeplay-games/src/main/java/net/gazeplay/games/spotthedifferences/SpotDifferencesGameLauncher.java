package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;

public class SpotDifferencesGameLauncher implements GameSpec.GameLauncher {

    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "spotthedifferences");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats) {
        return new SpotTheDifferences(gameContext, stats);
    }
}
