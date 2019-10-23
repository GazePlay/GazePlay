package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MemoryGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Memory").gameThumbnail("data/Thumbnails/memory.png").category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryGameVariantGenerator(), new MemoryGameLauncher());
    }
}
