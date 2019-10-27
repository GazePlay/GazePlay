package net.gazeplay.games.horses;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class HorsesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Horses").gameThumbnail("data/Thumbnails/horses.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new HorsesGameVariantGenerator(), new HorsesGameLauncher());
    }
}
