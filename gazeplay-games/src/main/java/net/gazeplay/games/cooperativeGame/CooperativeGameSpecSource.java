package net.gazeplay.games.cooperativeGame;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CooperativeGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("CooperativeGame").gameThumbnail("data/Thumbnails/chasedownChallenge.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new CooperativeGameVariantGenerator(), new CooperativeGameLauncher());
    }
}
