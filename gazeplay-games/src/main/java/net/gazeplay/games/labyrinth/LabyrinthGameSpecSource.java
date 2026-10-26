package net.gazeplay.games.labyrinth;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Labyrinth" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class LabyrinthGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Labyrinth").gameThumbnail("labyrinth")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION).build(),
            new LabyrinthGameVariantGenerator(), new LabyrinthGameLauncher());
    }
}
