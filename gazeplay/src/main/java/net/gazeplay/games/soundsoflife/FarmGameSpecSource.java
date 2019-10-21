package net.gazeplay.games.soundsoflife;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FarmGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Farm").gameThumbnail("data/Thumbnails/farm.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new FarmGameLauncher());
    }
}
