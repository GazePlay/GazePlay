package net.gazeplay.games.paperScissorsStone;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class PaperScissorsStoneGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Paper-Scissors-Stone").gameThumbnail("data/Thumbnails/PaperScissorsStone.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new PaperScissorsStoneLauncher());
    }
}
