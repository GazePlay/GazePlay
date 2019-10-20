package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.bubbles.Bubble;
import net.gazeplay.games.bubbles.BubbleType;
import net.gazeplay.games.bubbles.BubblesGamesStats;

public class ColoredBubblesGameLauncher implements GameSpec.GameLauncher {
	@Override
	public Stats createNewStats(Scene scene) {
		return new BubblesGamesStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
									   Stats stats) {
		return new Bubble(gameContext, BubbleType.COLOR, stats, true);
	}
}
