package net.gazeplay.games.ninja;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class NinjaGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary
                .builder()
                .nameCode("Ninja")
                .gameThumbnail("ninja")
                .category(GameCategories.Category.ACTION_REACTION)
                .absolutePriority(1)
                .build(),
            new NinjaGameVariantGenerator(), new NinjaGameLauncher());
    }
}
