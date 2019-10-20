package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class OpenMemoryNumbersGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("OpenMemoryNumbers", "data/Thumbnails/openMemoryNumbers.png",
				GameCategories.Category.ACTION_REACTION), new OpenMemoryNumbersGameVariantGenerator(), new OpenMemoryNumbersGameLauncher());
	}
}
