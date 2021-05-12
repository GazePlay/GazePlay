package net.gazeplay.games.memoryDynamicAdaptation;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class MemoryLettersGameSpecSourceDynamic implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MemoryLettersDynamic").gameThumbnail("data/Thumbnails/memory-letter.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION)
                .category(GameCategories.Category.LITERACY)
                .build(),
            new MemoryGameVariantGeneratorDynamic(), new MemoryLettersGameLauncherDynamic());
    }
}
