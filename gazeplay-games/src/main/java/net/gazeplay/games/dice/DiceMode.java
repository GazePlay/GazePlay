package net.gazeplay.games.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The two modes of "Dés" (maintainer's choice of 12/11/2026): count the points (a game: the dice roll, three cards show
 * numbers, the right total is chosen by a fixation), or throw the dice (a tool for a board game: 1 to 6 dice, the
 * total shown).
 */
@Getter
@RequiredArgsConstructor
public enum DiceMode {
    COUNT("DiceCount"),
    THROW("DiceThrow");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, counting for an unknown one
     */
    public static DiceMode of(final String key) {
        for (final DiceMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return COUNT;
    }
}
