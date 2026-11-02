package net.gazeplay.games.bubbles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Bubbles" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}). It replaces "ColoredBubbles" and "PortraitBubbles" (2/11/2026), now its variants.
 */
public class BubblesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Bubbles").gameThumbnail("bubblecolor")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new BubblesGameVariantGenerator(), new BubblesGameLauncher());
    }
}
