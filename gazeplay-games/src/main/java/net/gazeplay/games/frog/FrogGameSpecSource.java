package net.gazeplay.games.frog;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FrogGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary
                .builder()
                .nameCode("Frog")
                .gameThumbnail("frogsrace")
                .category(GameCategories.Category.ACTION_REACTION)
                .absolutePriority(2)
                .build(),
            new FrogGameLauncher());
    }
}
