package net.gazeplay.games.space;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Space Game" ends, besides the three lives lost (maintainer's rules of 3/11/2026): never, or the boss comes after
 * 1 or 3 minutes, or after 20 or 50 aliens destroyed, and the game is won when it is destroyed ("Bravo" and the
 * statistics). Without end, no boss.
 */
@Getter
@RequiredArgsConstructor
public enum SpaceEnd {
    ENDLESS("SpaceEndless", 0, 0),
    TIME_1("SpaceTime1", 0, 60),
    TIME_3("SpaceTime3", 0, 180),
    ALIENS_20("SpaceAliens20", 20, 0),
    ALIENS_50("SpaceAliens50", 50, 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The aliens to destroy before the boss, 0 when the end is not a number of aliens.
     */
    private final int aliens;

    /**
     * The time before the boss, in seconds, 0 when the end is not a time.
     */
    private final int seconds;

    /**
     * @return whether a boss comes (all the ends but without end)
     */
    public boolean hasBoss() {
        return this != ENDLESS;
    }

    /**
     * @return the end of a key, never for an unknown one
     */
    public static SpaceEnd of(final String key) {
        for (final SpaceEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
