package net.gazeplay.games.shooter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the shooting game ends (maintainer's choice of 2/11/2026: all of them, chosen in the variants): never, after
 * 50 or 100 targets shot, or after 1 or 3 minutes. Then "Bravo" and the statistics.
 */
@Getter
@RequiredArgsConstructor
public enum ShooterEnd {
    ENDLESS("ShooterEndless", 0, 0),
    SCORE_50("ShooterScore50", 50, 0),
    SCORE_100("ShooterScore100", 100, 0),
    TIME_1("ShooterTime1", 0, 60),
    TIME_3("ShooterTime3", 0, 180);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The targets to shoot, 0 when the end is not a score.
     */
    private final int targets;

    /**
     * The time of the game, in seconds, 0 when the end is not a time.
     */
    private final int seconds;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static ShooterEnd of(final String key) {
        for (final ShooterEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
