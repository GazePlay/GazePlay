package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class WhereIsTheNumberGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("WhereIsTheNumber", "data/Thumbnails/Where-is-the-Number.png",
				GameCategories.Category.MEMORIZATION), new WhereIsTheNumberGameVariantGenerator(), new WhereIsTheNumberGameLauncher());
	}
}
