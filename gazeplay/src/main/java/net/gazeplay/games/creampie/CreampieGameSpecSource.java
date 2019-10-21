package net.gazeplay.games.creampie;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class CreampieGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Creampie").gameThumbnail("data/Thumbnails/creamPie.png").category(GameCategories.Category.SELECTION).build(),
            new CreampieGameLauncher());
    }
}
