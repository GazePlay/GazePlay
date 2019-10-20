package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.games.math101.Math103GameLauncher;
import net.gazeplay.games.math101.Math103GameVariantGenerator;
import net.gazeplay.gameslocator.GameSpecSource;

public class Math103GameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(new GameSummary("Math103", "data/Thumbnails/math101.png",
				GameCategories.Category.LOGIC, null, "MathDescMult"), new Math103GameVariantGenerator(), new Math103GameLauncher());
	}
}
