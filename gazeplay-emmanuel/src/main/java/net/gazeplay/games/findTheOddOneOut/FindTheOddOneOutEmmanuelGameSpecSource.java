package net.gazeplay.games.findTheOddOneOut;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FindTheOddOneOutEmmanuelGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FindTheOddOneOut").gameThumbnail("findTheOddOneOut")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new FindTheOddOneOutEmmanuelGameVariantGenerator(), new FindTheOddOneOutEmmanuelGameLauncher());
    }
}
