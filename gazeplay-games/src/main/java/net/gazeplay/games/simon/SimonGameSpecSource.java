package net.gazeplay.games.simon;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.rushhour.RushHourGameLauncher;
import net.gazeplay.games.rushhour.RushHourGameVariant;

public class SimonGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Simon").gameThumbnail("data/Thumbnails/simon.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new SimonGameVariantGenerator(), new SimonGameLauncher());
    }
}
