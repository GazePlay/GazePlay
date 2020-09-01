package net.gazeplay.games.order;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class OrderGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new OrderStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant,
                                       Stats stats) {
        return new Order(gameContext, gameVariant.getNumber(), stats);
    }
}
