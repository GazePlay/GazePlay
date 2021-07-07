package net.gazeplay.games.samecolor;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SameColorSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Charlie").gameThumbnail("data/Thumbnails/Charlie.png")
                .category(GameCategories.Category.SELECTION)
                .build(),
            new SameColorVariantGenerator(), new SameColorLauncher());
    }
}
