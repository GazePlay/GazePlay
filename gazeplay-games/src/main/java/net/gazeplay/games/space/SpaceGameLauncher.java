package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class SpaceGameLauncher implements GameSpec.GameLauncher<SpaceGameStats, GameSpec.GameVariant> {

    @Override
    public SpaceGameStats createNewStats(Scene scene) {
        return new SpaceGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, SpaceGameStats stats) {
        return new SpaceGame(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, SpaceGameStats stats, double gameSeed) {
        return new SpaceGame(gameContext, stats, gameSeed);
    }

}
