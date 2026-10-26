package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "SurviveAgainstRobots" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class SurviveAgainstRobotsSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SurviveAgainstRobots").gameThumbnail("surviveAgainstRobots")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new SurviveAgainstRobotsVariantGenerator(), new SurviveAgainstRobotsLauncher());
    }

}
