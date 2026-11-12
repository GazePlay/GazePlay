package net.gazeplay.games.divisor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The levels of "Diviseur" (maintainer's choice of 12/11/2026): how many times the targets divide, and their speed, as
 * the seconds they take to go through the height of the screen.
 * <ul>
 * <li>Easy: 2 divisions, 7 targets (1 + 2 + 4), 5 s;</li>
 * <li>Normal: 3 divisions, 15 targets, 3 s (the number of targets of the game before its review);</li>
 * <li>Hard: 4 divisions, 31 targets, 2 s.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum DivisorLevel {
    EASY("Easy", 2, 5),
    NORMAL("Normal", 3, 3),
    HARD("Hard", 4, 2);

    /**
     * Key of translation, and value (the button) of the variants.
     */
    private final String key;

    /**
     * How many times a target divides in two, after the first one.
     */
    private final int divisions;

    /**
     * The seconds to go through the height of the screen.
     */
    private final double crossingSeconds;

    /**
     * @return the targets to burst: the first one, then twice as many at each division
     */
    public int getTargets() {
        return (1 << (divisions + 1)) - 1;
    }

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static DivisorLevel of(final String key) {
        for (final DivisorLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
