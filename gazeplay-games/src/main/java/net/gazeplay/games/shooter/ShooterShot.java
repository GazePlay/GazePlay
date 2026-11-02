package net.gazeplay.games.shooter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a target of the shooting game is shot (maintainer's choice of 2/11/2026: both, chosen in the variants): as soon as
 * the gaze enters it, or after a fixation (a ring goes round it meanwhile).
 */
@Getter
@RequiredArgsConstructor
public enum ShooterShot {
    AT_ONCE("ShooterAtOnce"),
    FIXATION("ShooterFixation");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at once for an unknown one
     */
    public static ShooterShot of(final String key) {
        for (final ShooterShot shot : values()) {
            if (shot.key.equals(key)) {
                return shot;
            }
        }
        return AT_ONCE;
    }
}
