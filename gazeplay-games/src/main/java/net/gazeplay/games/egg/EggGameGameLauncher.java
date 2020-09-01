package net.gazeplay.games.egg;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class EggGameGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new EggGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant,
                                       Stats stats) {
        return new EggGame(gameContext, stats, gameVariant.getNumber());
    }
}
