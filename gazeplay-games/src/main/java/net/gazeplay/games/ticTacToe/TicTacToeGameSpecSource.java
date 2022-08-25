package net.gazeplay.games.ticTacToe;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class TicTacToeGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("TicTacToe").gameThumbnail("data/Thumbnails/ticTacToe.png")
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.SELECTION)
                .build(),
            new TicTacToeGameVariantGenerator(), new TicTacToeGameLauncher());
    }
}
