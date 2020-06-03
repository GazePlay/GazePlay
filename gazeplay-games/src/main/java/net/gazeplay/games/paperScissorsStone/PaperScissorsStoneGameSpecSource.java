package net.gazeplay.games.paperScissorsStone;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class PaperScissorsStoneGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("PaperScissorsStone").gameThumbnail("data/Thumbnails/PaperScissorsStone.jpg").category(GameCategories.Category.SELECTION).build(),
            new PaperScissorsStoneLauncher());
    }
}
