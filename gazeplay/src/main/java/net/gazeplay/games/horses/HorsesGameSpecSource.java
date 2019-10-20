package net.gazeplay.games.horses;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class HorsesGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("Horses", "data/Thumbnails/horses.png", GameCategories.Category.ACTION_REACTION),
				new HorsesGameVariantGenerator(), new HorsesGameLauncher());
	}
}
