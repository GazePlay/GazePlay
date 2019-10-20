package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cups.CupsAndBalls;
import net.gazeplay.games.cups.utils.CupsAndBallsStats;

public class CupsBallsGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.CupsGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new CupsAndBallsStats(scene);
	}

	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.CupsGameVariant gameVariant,
									   Stats stats) {
		return new CupsAndBalls(gameContext, stats, gameVariant.getNoCups(), 3);
	}
}
