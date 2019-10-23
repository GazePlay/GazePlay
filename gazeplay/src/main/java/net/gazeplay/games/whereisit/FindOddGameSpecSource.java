package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class FindOddGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("findodd").gameThumbnail("data/Thumbnails/findtheodd.jpg").category(GameCategories.Category.MEMORIZATION).build(),
            new FindOddGameVariantGenerator(), new FindOddGameLauncher());
    }
}
