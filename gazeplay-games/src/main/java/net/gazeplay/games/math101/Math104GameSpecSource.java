package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class Math104GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math104").gameThumbnail("data/Thumbnails/math101.png").category(GameCategories.Category.LOGIC).description("MathDescDiv").build(),
            new Math104GameVariantGenerator(), new MathGameLauncher(Math101GameType.DIVISION));
    }
}
