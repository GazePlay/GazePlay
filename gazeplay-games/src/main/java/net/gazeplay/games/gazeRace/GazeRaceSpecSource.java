package net.gazeplay.games.gazeRace;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "GazeRace" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class GazeRaceSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("GazeRace").gameThumbnail("gazeRace")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new GazeRaceVariantGenerator(), new GazeRaceLauncher());
    }
}
