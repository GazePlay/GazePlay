package net.gazeplay.games.spotthedifferences;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "SpotDifference" to the menu: its name, thumbnail, categories, variants (easy, normal, hard, with
 * or without the ring of the fixation) and launcher (see {@code GameSpecSource}).
 */
public class SpotDifferencesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SpotDifference").gameThumbnail("spotthedifference")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new SpotTheDifferencesVariantGenerator(),
            new SpotDifferencesGameLauncher());
    }
}
