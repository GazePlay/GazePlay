package net.gazeplay.games.videogrid;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class VideoGridGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "videogrid");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats) {
        return new VideoGrid(gameContext, stats, gameVariant.getWidth(), gameVariant.getHeight());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats, double gameSeed) {
        return new VideoGrid(gameContext, stats, gameVariant.getWidth(), gameVariant.getHeight(), gameSeed);
    }
}
