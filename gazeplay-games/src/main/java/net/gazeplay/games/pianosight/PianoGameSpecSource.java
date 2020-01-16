package net.gazeplay.games.pianosight;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class PianoGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Piano").gameThumbnail("data/Thumbnails/pianosight.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new PianoGameLauncher());
    }
}
