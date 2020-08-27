package net.gazeplay.games.soundsoflife;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class SavannaGameLauncher implements IGameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new SoundsOfLifeStats(scene, "Savana");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new SoundsOfLife(gameContext, stats, 2);
    }
}
