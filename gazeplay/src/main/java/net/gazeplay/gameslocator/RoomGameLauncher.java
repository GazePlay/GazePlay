package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.room.Room;
import net.gazeplay.games.room.RoomStats;

public class RoomGameLauncher implements GameSpec.GameLauncher {

	@Override
	public Stats createNewStats(Scene scene) {
		return new RoomStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
									   Stats stats) {
		return new Room(gameContext, stats);
	}
}
