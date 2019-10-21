package net.gazeplay.games.order;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class OrderGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Order", "data/Thumbnails/ordre.png", GameCategories.Category.MEMORIZATION),
            new OrdersGameVariantGenerator(), new OrderGameLauncher());
    }
}
