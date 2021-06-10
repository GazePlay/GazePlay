package net.gazeplay.games.follow;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.commons.gamevariants.generators.SquareDimensionVariantGenerator;
import net.gazeplay.games.cakes.CakesGameVariantGenerator;

public class FollowGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Follow").gameThumbnail("data/Thumbnails/follow.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new FollowGameVariantGenerator(), new FollowGameLauncher());
    }
}
