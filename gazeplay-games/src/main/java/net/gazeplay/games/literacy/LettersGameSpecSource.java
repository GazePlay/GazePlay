package net.gazeplay.games.literacy;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class LettersGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Letters").gameThumbnail("letters")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new LettersGameVariantGenerator(), new LettersGameLauncher());
    }
}
