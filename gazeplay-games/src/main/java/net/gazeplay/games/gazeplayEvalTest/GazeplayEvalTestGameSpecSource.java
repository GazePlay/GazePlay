package net.gazeplay.games.gazeplayEvalTest;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class GazeplayEvalTestGameSpecSource implements  GameSpecSource{
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("GazePlayEvalTest")
                .gameThumbnail("data/Thumbnails/whereIsIt.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new GazeplayEvalTestGameLauncher());
    }
}
