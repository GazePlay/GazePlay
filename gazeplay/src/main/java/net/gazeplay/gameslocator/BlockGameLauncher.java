package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.blocs.Blocs;
import net.gazeplay.games.blocs.BlocsGamesStats;

public class BlockGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new BlocsGamesStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext,
									   GameSpec.DimensionGameVariant gameVariant, Stats stats) {
		return new Blocs(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), true, 1, false,
				stats);
	}
}
