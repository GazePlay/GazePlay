package net.gazeplay.games.spotthedifferences;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Declares the game "SpotDifference" to the menu: its name, thumbnail, categories, variants (easy, normal, hard) and
 * launcher (see {@code GameSpecSource}).
 */
public class SpotDifferencesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SpotDifference").gameThumbnail("spotthedifference")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new EnumGameVariantGenerator<>(SpotTheDifferencesVariant.values(), SpotTheDifferencesVariant::getLabel),
            new SpotDifferencesGameLauncher());
    }
}
