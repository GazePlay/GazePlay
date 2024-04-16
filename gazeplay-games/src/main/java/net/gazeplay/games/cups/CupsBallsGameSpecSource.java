package net.gazeplay.games.cups;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CupsBallsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("CupsBalls").gameThumbnail("passpass")
                .category(GameCategories.Category.MEMORIZATION)
                .category(GameCategories.Category.SELECTION).build(),
            new CupsBallsGameVariantGenerator(), new CupsBallsGameLauncher());
    }
}
