package net.gazeplay.games.space;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class SpaceGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("SpaceGame", "data/Thumbnails/space.png", GameCategories.Category.SELECTION),
				new SpaceGameLauncher());
	}
}
