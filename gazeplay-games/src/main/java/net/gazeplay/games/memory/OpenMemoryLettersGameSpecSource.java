package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "OpenMemoryLetters" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class OpenMemoryLettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("OpenMemoryLetters").gameThumbnail("openMemoryLetters")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION)
                .category(GameCategories.Category.LITERACY).build(),
            new MemoryGameVariantGenerator(), new OpenMemoryLettersGameLauncher());
    }
}
