package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class Math101DivisionGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math101: Division").priority(2).gameThumbnail("data/Thumbnails/math101.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).description("MathDescDiv").build(),
            new Math101DivisionGameVariantGenerator(), new MathGameLauncher(MathGameType.DIVISION));
    }
}
