package net.gazeplay.games.follow;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "Suivre le chemin" (name code Follow) to the menu: its name, thumbnail, category (action since its review of
 * 13/11/2026, logic and maths before), variants (see {@link FollowGameVariantGenerator}) and launcher (see
 * {@code GameSpecSource}).
 */
public class FollowGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Follow").gameThumbnail("follow")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new FollowGameVariantGenerator(), new FollowGameLauncher());
    }
}
