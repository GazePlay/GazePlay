package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class GooseGameLauncher implements GameSpec.GameLauncher<GooseGameStats, GameSpec.IntGameVariant> {
    @Override
    public GooseGameStats createNewStats(Scene scene) {
        return new GooseGameStats(scene, "goosegame");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.IntGameVariant gameVariant,
                                       GooseGameStats stats) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.IntGameVariant gameVariant,
                                       GooseGameStats stats, double gameSeed) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber(), gameSeed);
    }

}
