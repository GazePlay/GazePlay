package net.gazeplay.games.cups2;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CupsAndBallsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("AdaptiveCupsBalls").gameThumbnail("passpass")
                .category(GameCategories.Category.MEMORIZATION)
                .category(GameCategories.Category.SELECTION).build(),
            new CupsAndBallsGameVariantGenerator(), new CupsAndBallsGameLauncher());
    }
}
