package net.gazeplay.games.ninja;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class NinjaGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Ninja").gameThumbnail("data/Thumbnails/ninja.png").category(GameCategories.Category.SELECTION).build(),
            new NinjaGameVariantGenerator(), new NinjaGameLauncher());
    }
}
