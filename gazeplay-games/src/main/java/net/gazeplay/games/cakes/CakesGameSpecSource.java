package net.gazeplay.games.cakes;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class CakesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Cakes").gameThumbnail("data/Thumbnails/cakes.png").category(GameCategories.Category.MEMORIZATION).build(),
            new CakesGameVariantGenerator(), new CakesGameLauncher());
    }
}
