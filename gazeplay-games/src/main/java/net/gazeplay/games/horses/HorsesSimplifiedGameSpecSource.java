package net.gazeplay.games.horses;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Horses Simplified" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class HorsesSimplifiedGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Horses Simplified").gameThumbnail("horsesSimplified")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new HorsesSimplifiedGameVariantGenerator(), new HorsesSimplifiedGameLauncher());
    }
}
