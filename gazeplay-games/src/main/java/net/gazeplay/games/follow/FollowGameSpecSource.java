package net.gazeplay.games.follow;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "Follow" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class FollowGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Follow").gameThumbnail("follow").category(GameCategories.Category.LOGIC_MATHS).build(),
            new FollowGameVariantGenerator(), new FollowGameLauncher());
    }
}
