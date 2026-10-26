package net.gazeplay.games.memory;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "OpenMemory" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class OpenMemoryGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("OpenMemory").gameThumbnail("openMemory")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new MemoryGameVariantGenerator(), new OpenMemoryGameLauncher());
    }
}
