package net.gazeplay.games.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The levels of the counting mode of "Dés" (maintainer's choice of 12/11/2026): 1 die (1 to 6 points, recognizing
 * them), 2 dice (2 to 12) or 3 dice (3 to 18).
 */
@Getter
@RequiredArgsConstructor
public enum DiceLevel {
    EASY("DiceEasy", 1),
    NORMAL("DiceNormal", 2),
    HARD("DiceHard", 3);

    /**
     * Key of translation, and value (the button) of the variants.
     */
    private final String key;

    /**
     * The dice rolled.
     */
    private final int dice;

    /**
     * @return the level of a key, easy for an unknown one
     */
    public static DiceLevel of(final String key) {
        for (final DiceLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return EASY;
    }
}
