package net.gazeplay.games.divisor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class DivisorGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Divisor").gameThumbnail("data/Thumbnails/divisor.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new DivisorGameLauncher());
    }
}
