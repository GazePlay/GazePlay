package net.gazeplay.games.colors;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class ColorsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Colorsss").gameThumbnail("data/Thumbnails/colors.png").category(GameCategories.Category.ACTION_REACTION).description("ColorDesc").build(),
            new ColorsGameLauncher());
    }
}
