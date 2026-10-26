package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "ShootTheBiboules" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class ShootTheBiboulesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ShootTheBiboules").gameThumbnail("shootTheBiboules")
                .category(GameCategories.Category.ACTION_REACTION)
                .backgroundMusicUrl("https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3").build(),
            new ShootTheBiboulesGameLauncher());
    }
}
