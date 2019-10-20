package net.gazeplay.games.drawonvideo;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.drawonvideo.VideoPlayerWithLiveFeedbackApp;

public class VideoPlayerGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.StringGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new Stats(scene, "Video Player with Feedback");
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.StringGameVariant gameVariant,
									   Stats stats) {
		return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue());
	}
}
