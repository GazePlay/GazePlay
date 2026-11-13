package net.gazeplay.games.blocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Blocs" (maintainer's choice of 13/11/2026): the size of the grid of blocks over the picture (the
 * buttons of the variants).
 */
@Getter
@RequiredArgsConstructor
public enum BlocsLevel {
    EASY("BlocsEasy", 3),
    NORMAL("BlocsNormal", 5),
    HARD("BlocsHard", 8);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The blocks on a side of the grid.
     */
    private final int side;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static BlocsLevel of(final String key) {
        for (final BlocsLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }

    /**
     * @return the level of a grid of before the review (2 × 2 to 9 × 9), the nearest one
     */
    public static BlocsLevel of(final int side) {
        return side <= 3 ? EASY : side <= 6 ? NORMAL : HARD;
    }
}
