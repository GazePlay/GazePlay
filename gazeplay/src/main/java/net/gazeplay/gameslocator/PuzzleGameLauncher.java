package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.slidingpuzzle.slidingpuzzle;
import net.gazeplay.games.slidingpuzzle.slidingpuzzlestats;

public class PuzzleGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new slidingpuzzlestats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
									   Stats stats) {
		return new slidingpuzzle(stats, gameContext, 3, 3, gameVariant.getNumber());
	}
}
