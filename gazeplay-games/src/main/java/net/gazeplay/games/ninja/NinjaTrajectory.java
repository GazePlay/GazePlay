package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The way the fruit of "Ninja" goes (maintainer's choice of 4/11/2026, the ways of the game before its review): to
 * places drawn at random, or back and forth on a line. The keys are those of the variants of before, already translated.
 */
@Getter
@RequiredArgsConstructor
public enum NinjaTrajectory {
    RANDOM("Random"),
    VERTICAL("Vertical"),
    HORIZONTAL("Horizontal"),
    DIAGONAL_DOWN("Diagonal from upper left to lower right"),
    DIAGONAL_UP("Diagonal from upper right to lower left");

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at random for an unknown one
     */
    public static NinjaTrajectory of(final String key) {
        for (final NinjaTrajectory trajectory : values()) {
            if (trajectory.key.equals(key)) {
                return trajectory;
            }
        }
        return RANDOM;
    }
}
