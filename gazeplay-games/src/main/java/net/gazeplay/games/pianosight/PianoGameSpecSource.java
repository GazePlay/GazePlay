package net.gazeplay.games.pianosight;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Piano" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class PianoGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Piano").gameThumbnail("pianosight")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new PianoGameVariantGenerator(), new PianoGameLauncher());
    }
}
