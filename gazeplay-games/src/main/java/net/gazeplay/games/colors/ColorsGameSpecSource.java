package net.gazeplay.games.colors;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class ColorsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Coloring").gameThumbnail("data/Thumbnails/colors.png")
                .category(GameCategories.Category.MULTIMEDIA)
                .category(GameCategories.Category.SELECTION).description("ColorDesc").build(),
            new ColorsGameLauncher());
    }
}
