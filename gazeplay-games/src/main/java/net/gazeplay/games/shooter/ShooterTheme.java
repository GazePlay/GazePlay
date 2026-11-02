package net.gazeplay.games.shooter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The scene of the shooting game (2/11/2026: one card, the scene in the variants): Biboules flying out of a cage in a
 * classroom, shot with a magic wand, or robots coming out of a flying saucer above a street, shot with a gun.
 */
@Getter
@RequiredArgsConstructor
public enum ShooterTheme {
    BIBOULES("ShooterBiboules", "biboule"),
    ROBOTS("ShooterRobots", "robot");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * Folder of its pictures and sounds (data/shooter/&lt;folder&gt;), and name of its statistics (as before).
     */
    private final String folder;

    /**
     * @return the scene of a key, the Biboules for an unknown one
     */
    public static ShooterTheme of(final String key) {
        for (final ShooterTheme theme : values()) {
            if (theme.key.equals(key)) {
                return theme;
            }
        }
        return BIBOULES;
    }
}
