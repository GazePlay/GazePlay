package net.gazeplay.games.whereisit;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FindOddGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("findodd").gameThumbnail("data/Thumbnails/findtheodd.jpg").category(GameCategories.Category.MEMORIZATION).build(),
            new FindOddGameVariantGenerator(), new FindOddGameLauncher());
    }
}
