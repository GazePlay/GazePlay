package net.gazeplay.games.biboulejump;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When Biboule Jump ends (maintainer's choice of 4/11/2026): when the three lives are lost, or after 1 or 3 minutes if
 * they are not.
 */
@Getter
@RequiredArgsConstructor
public enum BibouleJumpEnd {
    ENDLESS("BibouleJumpEndless", 0),
    TIME_1("BibouleJumpTime1", 60),
    TIME_3("BibouleJumpTime3", 180);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The time of the game, in seconds, 0 when it has no limit of time.
     */
    private final int seconds;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static BibouleJumpEnd of(final String key) {
        for (final BibouleJumpEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
