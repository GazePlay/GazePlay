package net.gazeplay.games.blocs;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.commons.gamevariants.generators.SquareDimensionVariantGenerator;

/**
 * Declares the game "Blocks" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class BlocsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Blocks").gameThumbnail("block").category(GameCategories.Category.ACTION_REACTION).build(),
            new SquareDimensionVariantGenerator(2, 9), new BlocsGameLauncher());
    }
}
