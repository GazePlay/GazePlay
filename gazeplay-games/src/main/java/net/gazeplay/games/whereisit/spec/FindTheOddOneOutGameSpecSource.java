package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.FindTheOddOneOutGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.FindTheOddOneOutGameLauncher;

public class FindTheOddOneOutGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FindTheOddOneOut").gameThumbnail("data/Thumbnails/findTheOddOneOut.jpg")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new FindTheOddOneOutGameVariantGenerator(), new FindTheOddOneOutGameLauncher());
    }
}
