package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the shooting game to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}). It replaces "ShootTheBiboules" and "ShootTheRobots" (2/11/2026), now its scenes. Music:
 * "Talking Cute (Chiptune)" by Pro Sensory (Alex McCulloch), OpenGameArt, CC0.
 */
public class ShooterGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Shooter").gameThumbnail("shootTheBiboules")
                .category(GameCategories.Category.ACTION_REACTION)
                .backgroundMusicUrl("https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3").build(),
            new ShooterGameVariantGenerator(), new ShooterGameLauncher());
    }
}
