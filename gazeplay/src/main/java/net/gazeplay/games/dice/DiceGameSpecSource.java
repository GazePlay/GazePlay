package net.gazeplay.games.dice;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class DiceGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("Dice", "data/Thumbnails/dice.png", GameCategories.Category.MEMORIZATION),
				new DiceGameVariantGenerator(), new DiceGameLauncher());
	}
}
