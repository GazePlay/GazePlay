package net.gazeplay.games.ladder;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "Échelle" (name code Ladder) to the menu: its name, thumbnail, categories, variants (see
 * {@link LadderGameVariantGenerator}) and launcher (see {@code GameSpecSource}).
 */
public class LadderGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Ladder").gameThumbnail("ladder")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .build(),
            new LadderGameVariantGenerator(), new LadderGameLauncher());
    }
}
