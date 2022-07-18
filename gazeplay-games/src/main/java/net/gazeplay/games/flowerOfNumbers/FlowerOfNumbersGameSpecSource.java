package net.gazeplay.games.flowerOfNumbers;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FlowerOfNumbersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FlowerOfNumbers").gameThumbnail("data/Thumbnails/flowerOfNumbers.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .build(),
            new FlowerOfNumbersGameLauncher());
    }
}
