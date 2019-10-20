package net.gazeplay.games.biboulejump;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class BibouleJumpGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("BibJump", "data/Thumbnails/biboulejump.png", GameCategories.Category.SELECTION),
				new BibouleJumpGameVariantGenerator(), new BibouleJumpGameLauncher());
	}
}
