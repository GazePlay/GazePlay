package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The game of "Suivre le chemin" (13/11/2026, maintainer's rules): pick up the coins, or take the keys opening the doors
 * of their color, up to the treasure.
 */
@Getter
@RequiredArgsConstructor
public enum FollowMode {
    COINS("FollowCoins"),
    KEYS("FollowKeys");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the coins for an unknown one
     */
    public static FollowMode of(final String key) {
        for (final FollowMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return COINS;
    }
}
