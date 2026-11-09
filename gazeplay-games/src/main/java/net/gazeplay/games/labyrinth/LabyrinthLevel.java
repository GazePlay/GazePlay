package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of the Labyrinth (maintainer's choice of 9/11/2026): its size, in lines and columns of boxes.
 */
@Getter
@RequiredArgsConstructor
public enum LabyrinthLevel {
    EASY("LabyrinthEasy", 3, 5),
    NORMAL("LabyrinthNormal", 5, 7),
    HARD("LabyrinthHard", 7, 9);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    private final int lines;
    private final int columns;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static LabyrinthLevel of(final String key) {
        for (final LabyrinthLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
