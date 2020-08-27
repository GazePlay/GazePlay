package net.gazeplay.games.divisor;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class RabbitsGameLauncher implements IGameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new DivisorStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new Divisor(gameContext, stats, true);
    }
}
