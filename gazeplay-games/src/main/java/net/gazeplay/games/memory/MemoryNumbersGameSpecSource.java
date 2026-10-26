package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "MemoryNumbers" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class MemoryNumbersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MemoryNumbers").gameThumbnail("memoryNumbers")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryGameVariantGenerator(), new MemoryNumbersGameLauncher());
    }
}
