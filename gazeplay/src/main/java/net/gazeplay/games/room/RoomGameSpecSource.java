package net.gazeplay.games.room;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class RoomGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("Room", "data/Thumbnails/home.png", GameCategories.Category.ACTION_REACTION),
				new RoomGameLauncher());
	}
}
