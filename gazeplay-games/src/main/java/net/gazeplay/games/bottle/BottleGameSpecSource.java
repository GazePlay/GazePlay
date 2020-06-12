package net.gazeplay.games.bottle;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class BottleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("bottle").gameThumbnail("data/Thumbnails/supermarket.png").category(GameCategories.Category.SELECTION).build(),
            new BottleGameLauncher());
    }
}
