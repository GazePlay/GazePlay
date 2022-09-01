package net.gazeplay.games.divisor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class RabbitsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Rabbits").gameThumbnail("data/Thumbnails/rabbits.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new RabbitsGameLauncher());
    }
}
