package net.gazeplay.games.scratchcard;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "ScratchCard" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class ScratchCardGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ScratchCard").gameThumbnail("scratchcard")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new ScratchCardGameLauncher());
    }
}
