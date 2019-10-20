package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class BibouleGameSpecSource implements GameSpecSource {
	@Override
	public GameSpec getGameSpec() {
		return new GameSpec(
				new GameSummary("Biboule", "data/Thumbnails/biboules.png", GameCategories.Category.SELECTION,
						"https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3"),
				new ShooterGameLauncher());
	}
}
