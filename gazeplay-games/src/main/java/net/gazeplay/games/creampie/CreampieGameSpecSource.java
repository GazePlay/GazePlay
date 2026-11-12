package net.gazeplay.games.creampie;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "Tartes à la crème" (name code Creampie) to the menu: its name, thumbnail, categories, variants (see
 * {@link CreamPieGameVariantGenerator}) and launcher (see
 * {@code GameSpecSource}).
 */
public class CreampieGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary
                .builder()
                .nameCode("Creampie")
                .gameThumbnail("creamPie")
                .category(GameCategories.Category.ACTION_REACTION)
                .absolutePriority(2)
                .build(),
            new CreamPieGameVariantGenerator(), new CreampieGameLauncher());
    }
}
