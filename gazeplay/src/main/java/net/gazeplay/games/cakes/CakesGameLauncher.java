package net.gazeplay.games.cakes;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cakes.CakeFactory;
import net.gazeplay.games.cakes.CakeStats;

public class CakesGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new CakeStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
									   Stats stats) {
		return new CakeFactory(gameContext, stats, gameVariant.getNumber());
	}
}
