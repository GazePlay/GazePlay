package net.gazeplay.games.surviveAgainstRobots;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SurviveAgainstRobotsMouseSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SurviveAgainstRobotsMouse").gameThumbnail("data/Thumbnails/SurviveAgainstRobots.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new SurviveAgainstRobotsVariantGenerator(), new SurviveAgainstRobotsMouseLauncher());
    }
}
