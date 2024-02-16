package net.gazeplay.games.connect4;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class Connect4GameSpecSource  implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Connect4").gameThumbnail("connect4")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.ACTION_REACTION) .build(),
                new Connect4GameVariantGenerator(), new Connect4GameLauncher());
    }
}
