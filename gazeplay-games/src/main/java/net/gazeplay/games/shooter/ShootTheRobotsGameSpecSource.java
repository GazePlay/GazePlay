package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class ShootTheRobotsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ShootTheRobots").gameThumbnail("data/Thumbnails/shootTheRobots.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .backgroundMusicUrl("https://opengameart.org/sites/default/files/DST-TowerDefenseTheme_1.mp3").build(),
            new ShootTheRobotsGameLauncher());
    }
}
