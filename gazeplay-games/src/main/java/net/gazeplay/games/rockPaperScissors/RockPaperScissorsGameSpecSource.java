package net.gazeplay.games.rockPaperScissors;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class RockPaperScissorsGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("RockPaperScissors").gameThumbnail("rockPaperScissors")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new RockPaperScissorsGameVariantGenerator(), new RockPaperScissorsLauncher());
    }
}
