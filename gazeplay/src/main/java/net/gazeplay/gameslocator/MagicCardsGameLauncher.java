package net.gazeplay.gameslocator;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.magiccards.MagicCards;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;

public class MagicCardsGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
	@Override
	public Stats createNewStats(Scene scene) {
		return new MagicCardsGamesStats(scene);
	}

	@Override
	public GameLifeCycle createNewGame(GameContext gameContext,
									   GameSpec.DimensionGameVariant gameVariant, Stats stats) {
		return new MagicCards(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
	}
}
