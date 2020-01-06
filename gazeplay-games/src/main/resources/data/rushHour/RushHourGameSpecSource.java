package net.gazeplay.games.rushHour;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class RushHourGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("RushHour").gameThumbnail("data/Thumbnails/rushHour.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new RushHourGameLauncher());
    }
}
