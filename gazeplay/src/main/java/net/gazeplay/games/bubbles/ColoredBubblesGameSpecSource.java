package net.gazeplay.games.bubbles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class ColoredBubblesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ColoredBubbles").gameThumbnail("data/Thumbnails/bubblecolor.png").category(GameCategories.Category.SELECTION).build(),
            new ColoredBubblesGameLauncher());
    }
}
