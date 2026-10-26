package net.gazeplay.games.bottle;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Bottle" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class BottleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Bottle").gameThumbnail("bottle").category(GameCategories.Category.SELECTION).build(),
            new BottleGameVariantGenerator(), new BottleGameLauncher());
    }
}
