package net.gazeplay.games.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the counting mode of "Dés" ends (maintainer's choice of 12/11/2026, as in "Apprends les Maths"): after 5, 10 or
 * 20 totals found, or never. The throwing mode has no end.
 */
@Getter
@RequiredArgsConstructor
public enum DiceEnd {
    FOUND_5("DiceEnd5", 5),
    FOUND_10("DiceEnd10", 10),
    FOUND_20("DiceEnd20", 20),
    ENDLESS("DiceEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The totals to find, 0 without end.
     */
    private final int totals;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static DiceEnd of(final String key) {
        for (final DiceEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
