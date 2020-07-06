package net.gazeplay.games.colors;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class ColorsGameLauncher implements GameSpec.GameLauncher {

    private ColorsGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {

        gameStat = new ColorsGamesStats(scene);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats, double gameSeed) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator());
    }
}
