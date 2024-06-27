package net.gazeplay.games.follow2;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.follow2.FollowEmmanuelGameVariantGenerator;

public class FollowEmmanuelGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Follow").gameThumbnail("data/Thumbnails/follow.png").category(GameCategories.Category.LOGIC_MATHS).build(),
            new FollowEmmanuelGameVariantGenerator(), new FollowEmmanuelGameLauncher());
    }
}
