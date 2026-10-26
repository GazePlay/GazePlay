package net.gazeplay.games.draw;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Scribble" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class ScribbleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Scribble").gameThumbnail("gribouille")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new ScribbleGameLauncher());
    }
}
