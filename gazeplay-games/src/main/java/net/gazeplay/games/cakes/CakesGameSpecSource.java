package net.gazeplay.games.cakes;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CakesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Cakes").gameThumbnail("cakes")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION)
                .build(),
            new CakesGameVariantGenerator(), new CakesGameLauncher());
    }
}
