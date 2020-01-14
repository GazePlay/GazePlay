package net.gazeplay.games.egg;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class EggGameGameLauncher implements GameSpec.GameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new EggGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats) {
        return new EggGame(gameContext, stats);
    }
}
