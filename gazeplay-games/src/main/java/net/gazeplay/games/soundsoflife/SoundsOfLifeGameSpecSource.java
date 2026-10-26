package net.gazeplay.games.soundsoflife;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "SoundsOfLife" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class SoundsOfLifeGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SoundsOfLife").gameThumbnail("soundsOfLife")
                .category(GameCategories.Category.SELECTION)
                .build(),
            new SoundsOfLifeGameVariantGenerator(), new SoundsOfLifeGameLauncher());
    }
}
