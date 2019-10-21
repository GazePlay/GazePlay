package net.gazeplay.games.scratchcard;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class ScratchCardGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ScratchCard").gameThumbnail("data/Thumbnails/scratchcard.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new ScratchCardGameLauncher());
    }
}
