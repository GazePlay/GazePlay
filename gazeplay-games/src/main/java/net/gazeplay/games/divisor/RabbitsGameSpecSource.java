package net.gazeplay.games.divisor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Rabbits" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class RabbitsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Rabbits").gameThumbnail("rabbits")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new RabbitsGameLauncher());
    }
}
