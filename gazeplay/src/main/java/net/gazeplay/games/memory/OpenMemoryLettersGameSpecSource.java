package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.games.memory.OpenMemoryLettersGameLauncher;
import net.gazeplay.games.memory.OpenMemoryLettersGameVariantGenerator;
import net.gazeplay.gameslocator.GameSpecSource;

public class OpenMemoryLettersGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("OpenMemoryLetters", "data/Thumbnails/openMemoryLetters.png",
				GameCategories.Category.ACTION_REACTION), new OpenMemoryLettersGameVariantGenerator(), new OpenMemoryLettersGameLauncher());
	}
}
