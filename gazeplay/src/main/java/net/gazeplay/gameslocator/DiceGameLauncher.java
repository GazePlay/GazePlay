package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.dice.Dice;

public class DiceGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new Stats(scene, "dice");
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext,
									   GameSpec.IntGameVariant gameVariant, Stats stats) {
		return new Dice(gameContext, stats, gameVariant.getNumber());
	}

}
