package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.FindOddGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.FindOddGameLauncher;

public class FindOddGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("findodd").gameThumbnail("data/Thumbnails/findtheodd.jpg")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new FindOddGameVariantGenerator(), new FindOddGameLauncher());
    }
}
