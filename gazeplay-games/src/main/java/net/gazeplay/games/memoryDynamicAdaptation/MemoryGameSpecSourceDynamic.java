package net.gazeplay.games.memoryDynamicAdaptation;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class MemoryGameSpecSourceDynamic implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MemoryDynamic").gameThumbnail("data/Thumbnails/memory.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryGameVariantGeneratorDynamic(), new MemoryGameLauncherDynamic());
    }
}
