package net.gazeplay.games.order;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class OrderGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Order").gameThumbnail("ordre")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new OrdersGameVariantGenerator(), new OrderGameLauncher());
    }
}
