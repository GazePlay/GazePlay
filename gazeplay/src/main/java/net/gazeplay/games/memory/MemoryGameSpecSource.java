package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MemoryGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Memory", "data/Thumbnails/memory.png", GameCategories.Category.MEMORIZATION),
            new MemoryGameVariantGenerator(), new MemoryGameLauncher());
    }
}
