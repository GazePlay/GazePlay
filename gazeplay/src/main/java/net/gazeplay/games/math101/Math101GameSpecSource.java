package net.gazeplay.games.math101;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class Math101GameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(new GameSummary("Math101", "data/Thumbnails/math101.png",
            GameCategories.Category.LOGIC, null, "MathDescAdd"), new Math101GameVariantGenerator(), new Math101GameLauncher());
    }
}
