package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.soundsoflife.SoundsOfLife;
import net.gazeplay.games.soundsoflife.SoundsOfLifeStats;

public class SavannaGameLauncher implements GameSpec.GameLauncher {
	@Override
	public Stats createNewStats(Scene scene) {
		return new SoundsOfLifeStats(scene, "Savana");
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
									   Stats stats) {
		return new SoundsOfLife(gameContext, stats, 2);
	}
}
