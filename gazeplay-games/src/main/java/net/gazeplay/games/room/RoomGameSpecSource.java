package net.gazeplay.games.room;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class RoomGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Room").gameThumbnail("home").category(GameCategories.Category.ACTION_REACTION).build(),
            new RoomGameLauncher());
    }
}
