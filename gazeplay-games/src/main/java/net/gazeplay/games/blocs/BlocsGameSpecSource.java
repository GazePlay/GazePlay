package net.gazeplay.games.blocs;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "Blocs" (name code Blocks) to the menu: its name, thumbnail, categories, variants (see
 * {@link BlocsGameVariantGenerator}) and launcher (see {@code GameSpecSource}).
 */
public class BlocsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Blocks").gameThumbnail("block")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION)
                .build(),
            new BlocsGameVariantGenerator(), new BlocsGameLauncher());
    }
}
