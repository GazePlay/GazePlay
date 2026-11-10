package net.gazeplay.games.cups;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Gobelets Pass Pass" (maintainer's choices of 10/11/2026): the cups, the exchanges of two cups, and how
 * long an exchange lasts (in seconds, at the speed 1 of the settings).
 */
@Getter
@RequiredArgsConstructor
public enum CupsLevel {
    EASY("CupsEasy", 3, 5, 0.9),
    NORMAL("CupsNormal", 4, 8, 0.7),
    HARD("CupsHard", 5, 12, 0.5);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;
    private final int cups;
    private final int exchanges;
    private final double exchangeSeconds;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static CupsLevel of(final String key) {
        for (final CupsLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
