package net.gazeplay.games.slidingpuzzle;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class PuzzleGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("puzzle", "data/Thumbnails/slidingpuzzle.png", GameCategories.Category.ACTION_REACTION),
				new PuzzleGameVariantGenerator(), new PuzzleGameLauncher());
	}
}
