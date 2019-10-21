package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class WhereIsItGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("WhereIsIt", "data/Thumbnails/whereisit.png", GameCategories.Category.MEMORIZATION),
            new WhereIsItGameVariantGenerator(), new WhereIsItGameLauncher());
    }
}
