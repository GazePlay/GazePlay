package net.gazeplay.games.divisor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class RabbitsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Lapins", "data/Thumbnails/rabbits.png", GameCategories.Category.SELECTION),
            new RabbitsGameLauncher());
    }
}
