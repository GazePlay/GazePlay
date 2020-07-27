package net.gazeplay.games.colors;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class ColorsGameLauncher implements IGameLauncher {

    private ColorsGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {

        gameStat = new ColorsGamesStats(scene);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator());
    }
}
