package net.gazeplay.games.bubbles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class PortraitsBubblesGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("PortraitBubbles", "data/Thumbnails/bubble.png", GameCategories.Category.SELECTION),
				new PortraitBubblesGameLauncher());
	}
}
