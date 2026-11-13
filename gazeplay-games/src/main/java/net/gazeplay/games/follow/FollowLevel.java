package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Suivre le chemin" (13/11/2026): the boxes of the map (wide paths, then narrower ones), the share of the
 * walls opened again for loops (with the coins only: a door must not be gone round), the coins and the doors.
 */
@Getter
@RequiredArgsConstructor
public enum FollowLevel {
    EASY("FollowEasy", 3, 5, 0.5, 6, 1),
    NORMAL("FollowNormal", 4, 7, 0.25, 10, 2),
    HARD("FollowHard", 5, 9, 0.1, 14, 3);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    private final int lines;

    private final int columns;

    /**
     * The share of the walls still closed opened again, with the coins.
     */
    private final double loops;

    private final int coins;

    private final int doors;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static FollowLevel of(final String key) {
        for (final FollowLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
