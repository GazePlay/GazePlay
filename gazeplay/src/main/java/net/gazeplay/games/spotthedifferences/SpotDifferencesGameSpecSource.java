package net.gazeplay.games.spotthedifferences;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class SpotDifferencesGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("SpotDifference", "data/Thumbnails/spotthedifference.png",
				GameCategories.Category.ACTION_REACTION), new SpotDifferencesGameLauncher());
	}
}
