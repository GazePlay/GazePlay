package net.gazeplay.games.follow;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FollowGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Follow").gameThumbnail("follow").category(GameCategories.Category.LOGIC_MATHS).build(),
            new FollowGameVariantGenerator(), new FollowGameLauncher());
    }
}
