package net.gazeplay.games.ladder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Échelle" (13/11/2026): the lines of the ladder and the rungs between two lines (the buttons of the
 * variants).
 */
@Getter
@RequiredArgsConstructor
public enum LadderLevel {
    EASY("LadderEasy", 3, 2),
    NORMAL("LadderNormal", 4, 3),
    HARD("LadderHard", 5, 4);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    private final int lines;

    /**
     * The rungs between two neighbouring lines.
     */
    private final int rungs;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static LadderLevel of(final String key) {
        for (final LadderLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
