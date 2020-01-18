package net.gazeplay.games.cups;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CupsBallsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("CupsBalls").gameThumbnail("data/Thumbnails/passpass.png")
                .category(GameCategories.Category.MEMORIZATION)
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new CupsBallsGameVariantGenerator(), new CupsBallsGameLauncher());
    }
}
