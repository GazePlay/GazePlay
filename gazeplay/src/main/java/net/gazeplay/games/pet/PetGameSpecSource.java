package net.gazeplay.games.pet;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class PetGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("Pet", "data/Thumbnails/pet.png", GameCategories.Category.ACTION_REACTION),
				new PetGameLauncher());
	}
}
