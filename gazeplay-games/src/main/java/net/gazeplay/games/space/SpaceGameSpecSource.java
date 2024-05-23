package net.gazeplay.games.space;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SpaceGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SpaceGame").gameThumbnail("space").category(GameCategories.Category.ACTION_REACTION).build(),
            new SpaceGameVariantGenerator(),
            new SpaceGameLauncher());
    }
}
