package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class Math102GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(new GameSummary("Math102", "data/Thumbnails/math101.png",
            GameCategories.Category.LOGIC, null, "MathDescSub"), new Math102GameVariantGenerator(), new Math102GameLauncher());
    }
}
