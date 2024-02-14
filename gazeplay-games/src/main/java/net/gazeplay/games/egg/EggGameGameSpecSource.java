package net.gazeplay.games.egg;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class EggGameGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("EggGame").gameThumbnail("egg")
                .category(GameCategories.Category.SELECTION).build(),
            new EggGameVariantGenerator(), new EggGameGameLauncher());
    }
}
