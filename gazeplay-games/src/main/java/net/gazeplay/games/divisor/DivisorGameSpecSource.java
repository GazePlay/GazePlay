package net.gazeplay.games.divisor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Diviseur" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}). Since 12/11/2026, "Lapins" is one of its themes, no longer a game of its own.
 */
public class DivisorGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Divisor").gameThumbnail("divisor")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new DivisorGameVariantGenerator(), new DivisorGameLauncher());
    }
}
