package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class RobotsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Robots", "data/Thumbnails/robots.png", GameCategories.Category.SELECTION,
                "https://opengameart.org/sites/default/files/DST-TowerDefenseTheme_1.mp3"),
            new RobotsGameLauncher());
    }
}
