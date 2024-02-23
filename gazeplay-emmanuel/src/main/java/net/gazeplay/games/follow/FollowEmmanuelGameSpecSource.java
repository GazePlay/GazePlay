package net.gazeplay.games.follow;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class FollowEmmanuelGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FollowEmmanuel").gameThumbnail("follow").category(GameCategories.Category.LOGIC_MATHS).build(),
            new FollowEmmanuelGameVariantGenerator(), new FollowEmmanuelGameLauncher());
    }
}
