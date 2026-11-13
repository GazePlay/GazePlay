package net.gazeplay.games.colorblend;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "Mélanger les couleurs" (name code colorblend) to the menu: its name, thumbnail, categories, variants (see
 * {@link ColorBlendVariantGenerator}) and launcher (see {@code GameSpecSource}).
 */
public class ColorBlendSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("colorblend").gameThumbnail("colorblend")
                .category(GameCategories.Category.SELECTION)
                .build(),
            new ColorBlendVariantGenerator(), new ColorBlendLauncher());
    }
}
