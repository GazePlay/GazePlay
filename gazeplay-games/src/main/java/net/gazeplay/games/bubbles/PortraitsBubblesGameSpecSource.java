package net.gazeplay.games.bubbles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "PortraitBubbles" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class PortraitsBubblesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("PortraitBubbles").gameThumbnail("bubble").category(GameCategories.Category.ACTION_REACTION).build(),
            new BubbleGameVariantGenerator(), new PortraitBubblesGameLauncher());
    }
}
