package net.gazeplay.games.blocs;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class BlocsGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("Blocks", "data/Thumbnails/block.png", GameCategories.Category.ACTION_REACTION),
				new BlocsGameVariantGenerator(), new BlocsGameLauncher());
	}
}
