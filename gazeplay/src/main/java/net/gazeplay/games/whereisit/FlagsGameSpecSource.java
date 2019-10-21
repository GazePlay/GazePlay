package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FlagsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("flags", "data/Thumbnails/flags.png", GameCategories.Category.MEMORIZATION),
            new FlagsGameLauncher());
    }
}
