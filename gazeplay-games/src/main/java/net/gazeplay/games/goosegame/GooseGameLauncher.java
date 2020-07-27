package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;

public class GooseGameLauncher implements IGameLauncher<GooseGameStats, IntGameVariant> {
    @Override
    public GooseGameStats createNewStats(Scene scene) {
        return new GooseGameStats(scene, "goosegame");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant,
                                       GooseGameStats stats) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber());
    }

}
