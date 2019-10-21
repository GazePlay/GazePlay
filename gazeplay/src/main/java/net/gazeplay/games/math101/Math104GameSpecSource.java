package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class Math104GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(new GameSummary("Math104", "data/Thumbnails/math101.png",
            GameCategories.Category.LOGIC, null, "MathDescDiv"), new Math104GameVariantGenerator(), new Math104GameLauncher());
    }
}
