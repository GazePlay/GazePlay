package net.gazeplay.games.soundsoflife;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SeaGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Sea").gameThumbnail("data/Thumbnails/ocean.png")
                .category(GameCategories.Category.SELECTION).build(),
            new SeaGameLauncher());
    }
}
