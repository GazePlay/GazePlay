package net.gazeplay.games.cakes;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class CakesGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("Cakes", "data/Thumbnails/cakes.png", GameCategories.Category.MEMORIZATION),
				new CakesGameVariantGenerator(), new CakesGameLauncher());
	}
}
