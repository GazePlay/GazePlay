package net.gazeplay.games.space;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Space Game" (maintainer's rules of 3/11/2026): how many aliens at the same time, how often each of
 * them shoots, and the life of the boss.
 */
@Getter
@RequiredArgsConstructor
public enum SpaceLevel {
    EASY("SpaceEasy", 4, 6, 15),
    NORMAL("SpaceNormal", 6, 4, 30),
    HARD("SpaceHard", 8, 2.5, 50);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The aliens at the same time.
     */
    private final int aliens;

    /**
     * The time between two shots of an alien, in seconds (on average).
     */
    private final double shotPeriod;

    /**
     * The points of life of the boss: the shots it takes.
     */
    private final int bossLife;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static SpaceLevel of(final String key) {
        for (final SpaceLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
