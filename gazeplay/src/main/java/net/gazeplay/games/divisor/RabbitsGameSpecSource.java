package net.gazeplay.games.divisor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class RabbitsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Lapins").gameThumbnail("data/Thumbnails/rabbits.png").category(GameCategories.Category.SELECTION).build(),
            new RabbitsGameLauncher());
    }
}
