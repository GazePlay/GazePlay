package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.order.Order;
import net.gazeplay.games.order.OrderStats;

public class OrderGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.TargetsGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new OrderStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.TargetsGameVariant gameVariant,
									   Stats stats) {
		return new Order(gameContext, gameVariant.getNoTargets(), stats);
	}
}
