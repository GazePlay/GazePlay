package net.gazeplay.games.simon;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
/**
 * Declares the game "Simon" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class SimonGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Simon").gameThumbnail("simon")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new SimonGameVariantGenerator(), new SimonGameLauncher());
    }
}
