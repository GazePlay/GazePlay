package net.gazeplay.games.mediaPlayer;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MediaPlayerGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("MediaPlayer", "data/Thumbnails/gazeMedia.png", GameCategories.Category.ACTION_REACTION),
				new MediaPlayerGameLauncher());
	}
}
