package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.biboulejump.BibouleJump;
import net.gazeplay.games.biboulejump.BibouleJumpStats;

public class BibouleJumpGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new BibouleJumpStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
									   Stats stats) {
		return new BibouleJump(gameContext, stats, gameVariant.getNumber());
	}

}
