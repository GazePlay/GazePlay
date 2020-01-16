package net.gazeplay.games.creampie;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CreampieGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Creampie").gameThumbnail("data/Thumbnails/creamPie.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new CreampieGameLauncher());
    }
}
