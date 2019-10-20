package net.gazeplay.games.bubbles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class ColoredBubblesGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("ColoredBubbles", "data/Thumbnails/bubblecolor.png", GameCategories.Category.SELECTION),
				new ColoredBubblesGameLauncher());
	}
}
