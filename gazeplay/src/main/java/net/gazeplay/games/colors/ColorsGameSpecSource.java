package net.gazeplay.games.colors;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class ColorsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Colorsss", "data/Thumbnails/colors.png", GameCategories.Category.ACTION_REACTION, null,
                "ColorDesc"), new ColorsGameLauncher());
    }
}
