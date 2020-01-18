package net.gazeplay.games.slidingpuzzle;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class PuzzleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("puzzle").gameThumbnail("data/Thumbnails/slidingpuzzle.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new PuzzleGameVariantGenerator(), new PuzzleGameLauncher());
    }
}
