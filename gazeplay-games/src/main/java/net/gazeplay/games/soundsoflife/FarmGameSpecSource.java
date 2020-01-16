package net.gazeplay.games.soundsoflife;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FarmGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Farm").gameThumbnail("data/Thumbnails/farm.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION).build(),
            new FarmGameLauncher());
    }
}
