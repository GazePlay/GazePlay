package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class Math101AllOperationsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math101: All operations").priority(1).gameThumbnail("math101")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).description("MathDesc").build(),
            new Math101AllOperationsGameVariantGenerator(), new MathGameLauncher(MathGameType.MATHALL));
    }
}
