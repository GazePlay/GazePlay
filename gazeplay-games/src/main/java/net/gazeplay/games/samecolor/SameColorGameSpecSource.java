package net.gazeplay.games.samecolor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SameColorGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SameColor").gameThumbnail("data/Thumbnails/SameColor.png")
                .category(GameCategories.Category.SELECTION)
                .build(),
            new SameColorGameVariantGenerator(), new SameColorGameLauncher());
    }
}
