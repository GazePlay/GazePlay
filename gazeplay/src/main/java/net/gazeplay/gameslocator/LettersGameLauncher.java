package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.literacy.Letters;
import net.gazeplay.games.literacy.LettersGamesStats;

public class LettersGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new LettersGamesStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext,
									   GameSpec.DimensionGameVariant gameVariant, Stats stats) {
		return new Letters(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
	}
}
