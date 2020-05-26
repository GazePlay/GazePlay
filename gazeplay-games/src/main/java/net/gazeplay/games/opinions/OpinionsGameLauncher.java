package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class OpinionsGameLauncher implements GameSpec.GameLauncher<OpinionsGameStats, GameSpec.GameVariant> {

    @Override
    public OpinionsGameStats createNewStats(Scene scene) {
        return new OpinionsGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, OpinionsGameStats stats) {
        return new OpinionsGame(gameContext, stats);
    }

}
