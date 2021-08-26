package net.gazeplay.games.ladder;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class LadderGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Ladder").gameThumbnail("data/Thumbnails/cakes.png")
                .category(GameCategories.Category.SELECTION)
                .build(),
            new LadderGameLauncher());
    }
}
