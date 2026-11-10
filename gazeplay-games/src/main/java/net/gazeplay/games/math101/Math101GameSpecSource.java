package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "Apprends les Maths" (name code Math101) to the menu: its name, thumbnail, categories, variants and launcher
 * (see {@code GameSpecSource}). One game since the review of 10/11/2026 (maintainer's choice): before, 5 games
 * ("Math101: Addition", "Math101: Substraction", "Math101: Multiplication", "Math101: Division" and "Math101: All
 * operations"), now its topics.
 */
public class Math101GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math101").gameThumbnail("math101")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new Math101GameVariantGenerator(), new MathGameLauncher());
    }
}
