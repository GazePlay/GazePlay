package net.gazeplay.games.drawonvideo;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.StringGameVariant;
import net.gazeplay.stats.ExplorationGamesStats;

public class VideoPlayerGameLauncher implements IGameLauncher<ExplorationGamesStats, StringGameVariant> {
    @Override
    public ExplorationGamesStats createNewStats(Scene scene) {
        return new ExplorationGamesStats(scene, "Video Player with Feedback");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, StringGameVariant gameVariant,
                                       ExplorationGamesStats stats) {
        return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue());
    }
}
