package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class Math103GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math103").gameThumbnail("data/Thumbnails/math101.png").category(GameCategories.Category.LOGIC).description("MathDescMult").build(),
            new Math103GameVariantGenerator(), new MathGameLauncher(Math101GameType.MULTIPLICATION));
    }
}
