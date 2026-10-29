package net.gazeplay.games.colors;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Coloring" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class ColorsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Coloring").gameThumbnail("colors")
                .category(GameCategories.Category.MULTIMEDIA)
                .category(GameCategories.Category.SELECTION).description("ColorDesc").build(),
            new ColorsGameVariantGenerator(), new ColorsGameLauncher());
    }
}
