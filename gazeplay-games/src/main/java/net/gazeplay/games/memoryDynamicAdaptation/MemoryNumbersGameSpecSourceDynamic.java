package net.gazeplay.games.memoryDynamicAdaptation;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class MemoryNumbersGameSpecSourceDynamic implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MemoryNumbersDynamic").gameThumbnail("data/Thumbnails/memory-numbers.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryGameVariantGeneratorDynamic(), new MemoryNumbersGameLauncherDynamic());
    }
}
