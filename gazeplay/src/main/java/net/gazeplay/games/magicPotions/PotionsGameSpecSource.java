package net.gazeplay.games.magicPotions;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class PotionsGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("Potions", "data/Thumbnails/potions.jpg", GameCategories.Category.SELECTION),
				new PotionsGameLauncher());
	}
}
