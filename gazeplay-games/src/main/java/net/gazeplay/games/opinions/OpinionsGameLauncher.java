package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;

public class OpinionsGameLauncher implements IGameLauncher<OpinionsGameStats, IGameVariant> {

    @Override
    public OpinionsGameStats createNewStats(Scene scene) {
        return new OpinionsGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, OpinionsGameStats stats) {
        return new OpinionsGame(gameContext, stats);
    }

}
