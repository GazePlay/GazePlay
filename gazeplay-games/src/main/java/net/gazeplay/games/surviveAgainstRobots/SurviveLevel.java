package net.gazeplay.games.surviveAgainstRobots;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Survive Against Robots" (maintainer's rules of 3/11/2026): how many robots at most at the same time,
 * and how many of them shoot (the red ones).
 */
@Getter
@RequiredArgsConstructor
public enum SurviveLevel {
    EASY("SurviveEasy", 2, 0),
    NORMAL("SurviveNormal", 3, 1.0 / 5),
    HARD("SurviveHard", 4, 1.0 / 3);

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * The robots at most at the same time (one at first, one more each {@link SurviveAgainstRobots#MORE_ROBOTS_EACH}
     * robots destroyed).
     */
    private final int robots;

    /**
     * The part of the robots that shoot.
     */
    private final double shooters;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static SurviveLevel of(final String key) {
        for (final SurviveLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
