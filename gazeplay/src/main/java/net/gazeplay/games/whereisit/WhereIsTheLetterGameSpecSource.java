package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.WhereIsTheLetterGameLauncher;
import net.gazeplay.games.whereisit.WhereIsTheLetterGameVariantGenerator;
import net.gazeplay.gameslocator.GameSpecSource;

public class WhereIsTheLetterGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("WhereIsTheLetter", "data/Thumbnails/Where-is-the-Letter.png",
				GameCategories.Category.MEMORIZATION), new WhereIsTheLetterGameVariantGenerator(), new WhereIsTheLetterGameLauncher());
	}
}
