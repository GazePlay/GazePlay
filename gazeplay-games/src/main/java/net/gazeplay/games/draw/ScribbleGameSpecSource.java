package net.gazeplay.games.draw;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class ScribbleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Scribble").gameThumbnail("data/Thumbnails/gribouille.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new ScribbleGameLauncher());
    }
}
