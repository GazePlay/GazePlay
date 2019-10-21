package net.gazeplay.games.order;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;

public class OrderGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.TargetsGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new OrderStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.TargetsGameVariant gameVariant,
                                       Stats stats) {
        return new Order(gameContext, gameVariant.getNoTargets(), stats);
    }
}
