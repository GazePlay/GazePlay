package net.gazeplay.games.cups;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "CupsBalls" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
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
