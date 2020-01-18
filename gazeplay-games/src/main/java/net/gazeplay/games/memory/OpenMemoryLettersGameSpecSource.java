package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class OpenMemoryLettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("OpenMemoryLetters").gameThumbnail("data/Thumbnails/openMemoryLetters.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION)
                .category(GameCategories.Category.LITERACY).build(),
            new MemoryGameVariantGenerator(), new OpenMemoryLettersGameLauncher());
    }
}
