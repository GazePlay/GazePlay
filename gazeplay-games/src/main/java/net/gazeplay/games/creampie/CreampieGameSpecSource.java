package net.gazeplay.games.creampie;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Creampie" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class CreampieGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary
                .builder()
                .nameCode("Creampie")
                .gameThumbnail("creamPie")
                .category(GameCategories.Category.ACTION_REACTION)
                .absolutePriority(2)
                .build(),
            new CreampieGameLauncher());
    }
}
