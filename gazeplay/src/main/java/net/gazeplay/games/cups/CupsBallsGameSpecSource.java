package net.gazeplay.games.cups;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class CupsBallsGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("CupsBalls", "data/Thumbnails/passpass.png", GameCategories.Category.MEMORIZATION),
				new CupsBallsGameVariantGenerator(), new CupsBallsGameLauncher());
	}
}
