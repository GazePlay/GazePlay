package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class Math102GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math102").gameThumbnail("data/Thumbnails/math101.png").category(GameCategories.Category.LOGIC).description("MathDescSub").build(),
            new Math102GameVariantGenerator(), new Math102GameLauncher());
    }
}
