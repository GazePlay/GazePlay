package net.gazeplay.games.sprintFinish;

import lombok.Getter;

/**
 * The difficulty of "Sprint Finish" (maintainer's rules of 2/11/2026): the number of maps to win, one variant each.
 */
@Getter
public enum SprintDifficulty {

    EASY("SprintEasy", 5),
    NORMAL("SprintNormal", 10),
    HARD("SprintHard", 15);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The number of maps to win.
     */
    private final int maps;

    SprintDifficulty(final String key, final int maps) {
        this.key = key;
        this.maps = maps;
    }

    /**
     * @return the difficulty of a key, normal for an unknown one
     */
    public static SprintDifficulty of(final String key) {
        for (final SprintDifficulty difficulty : values()) {
            if (difficulty.key.equals(key)) {
                return difficulty;
            }
        }
        return NORMAL;
    }
}
