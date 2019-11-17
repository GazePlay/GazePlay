package net.gazeplay.games.spotthedifferences;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SpotDifferencesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SpotDifference").gameThumbnail("data/Thumbnails/spotthedifference.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new SpotDifferencesGameLauncher());
    }
}
