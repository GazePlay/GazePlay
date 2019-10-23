package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MemoryNumbersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MemoryNumbers").gameThumbnail("data/Thumbnails/memory-numbers.png").category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryNumbersGameVariantGenerator(), new MemoryNumbersGameLauncher());
    }
}
