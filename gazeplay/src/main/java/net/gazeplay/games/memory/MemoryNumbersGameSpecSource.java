package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MemoryNumbersGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("MemoryNumbers", "data/Thumbnails/memory-numbers.png",
				GameCategories.Category.MEMORIZATION), new MemoryNumbersGameVariantGenerator(), new MemoryNumbersGameLauncher());
	}
}
