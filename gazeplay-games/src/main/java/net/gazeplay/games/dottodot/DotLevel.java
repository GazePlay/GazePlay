package net.gazeplay.games.dottodot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The levels of "Point par point" (maintainer's choice of 12/11/2026): easy figures (4 to 7 dots, the next dot pulses
 * to help), normal (8 to 12) and hard (13 to 20, no help), or progressive: it starts easy and goes to the next level
 * after 3 drawings in a row without a mistake.
 */
@Getter
@RequiredArgsConstructor
public enum DotLevel {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard"),
    PROGRESSIVE("DotProgressive");

    /**
     * Key of translation, and value (the button) of the variants.
     */
    private final String key;

    /**
     * Drawings in a row without a mistake before the progressive level goes up.
     */
    public static final int PROGRESS_AFTER = 3;

    /**
     * @return the level of a key, easy for an unknown one
     */
    public static DotLevel of(final String key) {
        for (final DotLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return EASY;
    }
}
