package net.gazeplay.games.race;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "FrogsRace" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class FrogRaceGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FrogsRace").gameThumbnail("frogsrace")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new FrogsRaceGameLauncher());
    }
}
