package net.gazeplay.games.ninja;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class NinjaGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("Ninja", "data/Thumbnails/ninja.png", GameCategories.Category.SELECTION),
				new NinjaGameVariantGenerator(), new NinjaGameLauncher());
	}
}
