package net.gazeplay.games.mediaPlayer;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.pet.PetStats;
import net.gazeplay.stats.ExplorationGamesStats;

public class MediaPlayerGameLauncher implements GameSpec.GameLauncher<ExplorationGamesStats, GameSpec.DimensionGameVariant> {
    @Override
    public ExplorationGamesStats createNewStats(Scene scene) {
        return new ExplorationGamesStats(scene,"mediaPlayer");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, ExplorationGamesStats stats) {
        return new GazeMediaPlayer(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, ExplorationGamesStats stats, double gameSeed) {
        return new GazeMediaPlayer(gameContext, stats);
    }

}
