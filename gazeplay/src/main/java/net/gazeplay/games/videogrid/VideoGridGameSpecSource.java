package net.gazeplay.games.videogrid;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class VideoGridGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("VideoGrid", "data/Thumbnails/videogrid.png", GameCategories.Category.SELECTION),
				new VideoGridGameVariantGenerator(), new VideoGridGameLauncher());
	}
}
