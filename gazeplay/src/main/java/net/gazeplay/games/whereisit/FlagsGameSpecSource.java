package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FlagsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("flags").gameThumbnail("data/Thumbnails/flags.png").category(GameCategories.Category.MEMORIZATION).build(),
            new FlagsGameLauncher());
    }
}
