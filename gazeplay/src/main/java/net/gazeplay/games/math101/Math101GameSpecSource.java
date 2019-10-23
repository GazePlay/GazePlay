package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class Math101GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Math101").gameThumbnail("data/Thumbnails/math101.png").category(GameCategories.Category.LOGIC).description("MathDescAdd").build(),
            new Math101GameVariantGenerator(), new Math101GameLauncher());
    }
}
