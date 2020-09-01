package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;

public class SpaceGameLauncher implements IGameLauncher<SpaceGameStats, IGameVariant> {

    @Override
    public SpaceGameStats createNewStats(Scene scene) {
        return new SpaceGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, SpaceGameStats stats) {
        return new SpaceGame(gameContext, stats);
    }

}
