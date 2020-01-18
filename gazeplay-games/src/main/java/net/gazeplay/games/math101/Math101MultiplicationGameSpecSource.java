package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class Math101MultiplicationGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math101: Multiplication").priority(3).gameThumbnail("data/Thumbnails/math101.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).description("MathDescMult").build(),
            new Math101MultiplicationGameVariantGenerator(), new MathGameLauncher(MathGameType.MULTIPLICATION));
    }
}
