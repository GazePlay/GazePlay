package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Casse-brique" (maintainer's rules of 3/11/2026): the width of the paddle, the speed of the ball, and
 * whether some bricks must be hit twice.
 */
@Getter
@RequiredArgsConstructor
public enum CasseBriqueLevel {
    EASY("CasseBriqueEasy", 0.30, 0.55, false),
    NORMAL("CasseBriqueNormal", 0.22, 0.75, false),
    HARD("CasseBriqueHard", 0.16, 0.95, true);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The width of the paddle, in widths of the screen.
     */
    private final double paddleWidth;

    /**
     * The speed of the ball, in heights of the screen per second, at the normal animation speed.
     */
    private final double ballSpeed;

    /**
     * Whether one brick in three must be hit twice (it cracks at the first hit).
     */
    private final boolean strongBricks;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static CasseBriqueLevel of(final String key) {
        for (final CasseBriqueLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
