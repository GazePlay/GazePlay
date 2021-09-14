package net.gazeplay.games.dottodot;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class DotToDotGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary
                .builder()
                .nameCode("DotToDot")
                .gameThumbnail("data/Thumbnails/dottodot.png")
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.SELECTION)
                .absolutePriority(1)
                .build(),
            new DotToDotGameVariantGenerator(), new DotToDotGameLauncher()
        );
    }
}
