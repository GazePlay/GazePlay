package net.gazeplay.games.bubbles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class ColoredBubblesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ColoredBubbles").gameThumbnail("data/Thumbnails/bubblecolor.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new BubbleGameVariantGenerator(), new ColoredBubblesGameLauncher());
    }
}
