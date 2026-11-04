package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

import java.net.URL;

/**
 * Declares the shooting game to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}). It replaces "ShootTheBiboules" and "ShootTheRobots" (2/11/2026), now its scenes. Music:
 * "Talking Cute (Chiptune)" by Pro Sensory (Alex McCulloch), OpenGameArt, CC0, delivered with the game since 4/11/2026
 * (maintainer's decision): it used to be downloaded at the first game, so that there was no music without internet.
 */
public class ShooterGameSpecSource implements GameSpecSource {

    private static final String MUSIC = "data/shooter/music/TalkingCuteChiptune.mp3";

    @Override
    public GameSpec getGameSpec() {
        // The music manager copies it from the jar to its cache: JavaFX cannot play a music inside a jar
        final URL music = ShooterGameSpecSource.class.getClassLoader().getResource(MUSIC);
        return new GameSpec(
            GameSummary.builder().nameCode("Shooter").gameThumbnail("shootTheBiboules")
                .category(GameCategories.Category.ACTION_REACTION)
                .backgroundMusicUrl(music == null ? null : music.toExternalForm()).build(),
            new ShooterGameVariantGenerator(), new ShooterGameLauncher());
    }
}
