package net.gazeplay.games.horses;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.horses.Horses;

public class HorsesSimplifiedGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new Stats(scene, "horsesSimplified");
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
									   Stats stats) {
		return new Horses(gameContext, stats, 1, gameVariant.getNumber());
	}

}
