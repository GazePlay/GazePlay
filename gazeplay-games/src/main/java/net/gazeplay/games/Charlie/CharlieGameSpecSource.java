package net.gazeplay.games.Charlie;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CharlieGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Charlie").gameThumbnail("data/Thumbnails/Charlie.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION)
                .build(),
            new CharlieGameVariantGenerator(), new CharlieGameLauncher());
    }
}

