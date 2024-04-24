package net.gazeplay.games.colorblend;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class ColorBlendSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(GameSummary.builder().nameCode("colorblend").gameThumbnail("colorblend")
            .category(GameCategories.Category.SELECTION)
            .category(GameCategories.Category.MULTIMEDIA).description("ColorBlendDesc").build(),
        new ColorBlendLauncher());
    }
}
