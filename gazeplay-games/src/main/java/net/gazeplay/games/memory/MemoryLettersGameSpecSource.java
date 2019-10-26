package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class MemoryLettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MemoryLetters").gameThumbnail("data/Thumbnails/memory-letter.png").category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryLettersGameVariantGenerator(), new MemoryLettersGameLauncher());
    }
}
