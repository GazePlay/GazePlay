package net.gazeplay.games.biboulejump;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of Biboule Jump (maintainer's choice of 4/11/2026, in place of "with or without moving platforms"): the width
 * of the clouds, how far apart they are, how many of them move, and how slow the jumps are (26/11/2026). Every cloud can
 * be reached from the one under it.
 */
@Getter
@RequiredArgsConstructor
public enum BibouleJumpLevel {
    EASY("BibouleJumpEasy", 1.0 / 5, 0.20, 0.30, 0.25, 0, 1.6),
    NORMAL("BibouleJumpNormal", 1.0 / 7, 0.22, 0.40, 0.28, 0.25, 1.25),
    HARD("BibouleJumpHard", 1.0 / 9, 0.25, 0.45, 0.30, 0.5, 1);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The width of a cloud, in parts of the width of the screen.
     */
    private final double cloudWidth;

    /**
     * The height between two clouds, in parts of the height of the screen.
     */
    private final double minGap;
    private final double maxGap;

    /**
     * How far sideways the next cloud can be, in parts of the width of the screen.
     */
    private final double maxShift;

    /**
     * The chance that a cloud moves.
     */
    private final double movingChance;

    /**
     * How many times slower the jumps and the falls are, as high as before (tester's feedback of 23/11/2026, point 52;
     * maintainer's choice of 26/11/2026: by the level); the Biboule goes sideways as fast at every level.
     */
    private final double jumpSlowness;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static BibouleJumpLevel of(final String key) {
        for (final BibouleJumpLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
