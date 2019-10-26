package net.gazeplay.games.room;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class RoomGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Room").gameThumbnail("data/Thumbnails/home.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new RoomGameLauncher());
    }
}
