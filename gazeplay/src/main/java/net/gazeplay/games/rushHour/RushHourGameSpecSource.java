package net.gazeplay.games.rushHour;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class RushHourGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("RushHour", "data/Thumbnails/rushHour.png", GameCategories.Category.ACTION_REACTION),
				new RushHourGameLauncher());
	}
}
