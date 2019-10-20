package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.WhereIsTheAnimalGameLauncher;
import net.gazeplay.games.whereisit.WhereIsTheAnimalGameVariantGenerator;
import net.gazeplay.gameslocator.GameSpecSource;

public class WhereIsTheAnimalGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("WhereIsTheAnimal", "data/Thumbnails/whereisanimal.png",
				GameCategories.Category.MEMORIZATION), new WhereIsTheAnimalGameVariantGenerator(), new WhereIsTheAnimalGameLauncher());
	}
}
