package net.gazeplay.games.horses;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class HorsesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Horses").gameThumbnail("horses")
                .category(GameCategories.Category.MULTIMEDIA)
                .category(GameCategories.Category.SELECTION).build(),
            new HorsesGameVariantGenerator(), new HorsesGameLauncher());
    }
}
