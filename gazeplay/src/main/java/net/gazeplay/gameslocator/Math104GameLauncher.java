package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.math101.Math101;
import net.gazeplay.games.math101.MathGamesStats;

public class Math104GameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new MathGamesStats(scene);
	}// Need to make customized stats

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
									   Stats stats) {
		return new Math101(Math101.Math101GameType.DIVISION, gameContext, gameVariant.getNumber(),
				stats);
	}
}
