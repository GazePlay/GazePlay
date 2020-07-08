package net.gazeplay.games.mediaPlayer;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.stats.ExplorationGamesStats;

public class MediaPlayerGameLauncher implements GameSpec.GameLauncher<ExplorationGamesStats, DimensionGameVariant> {
    @Override
    public ExplorationGamesStats createNewStats(Scene scene) {
        return new ExplorationGamesStats(scene, "mediaPlayer");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionGameVariant gameVariant, ExplorationGamesStats stats) {
        return new GazeMediaPlayer(gameContext, stats);
    }

}
