package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FindOddGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("findodd", "data/Thumbnails/findtheodd.jpg", GameCategories.Category.MEMORIZATION),
            new FindOddGameVariantGenerator(), new FindOddGameLauncher());
    }
}
