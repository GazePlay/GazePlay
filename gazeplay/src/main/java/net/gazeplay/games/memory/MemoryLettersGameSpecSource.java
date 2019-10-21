package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MemoryLettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(new GameSummary("MemoryLetters", "data/Thumbnails/memory-letter.png",
            GameCategories.Category.MEMORIZATION), new MemoryLettersGameVariantGenerator(), new MemoryLettersGameLauncher());
    }
}
