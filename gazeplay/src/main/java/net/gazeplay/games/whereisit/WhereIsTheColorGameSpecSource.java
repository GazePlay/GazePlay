package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.WhereIsTheColorGameLauncher;
import net.gazeplay.games.whereisit.WhereIsTheColorGameVariantGenerator;
import net.gazeplay.gameslocator.GameSpecSource;

public class WhereIsTheColorGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("WhereIsTheColor", "data/Thumbnails/whereiscolor.png",
				GameCategories.Category.MEMORIZATION), new WhereIsTheColorGameVariantGenerator(), new WhereIsTheColorGameLauncher());
	}
}
