package net.gazeplay.games.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What a wrong total does in the counting mode of "Dés" (maintainer's choice of 12/11/2026, as in "Apprends les
 * Maths"): the card is greyed and the player looks again until the right total is found (no lives), or one of 3 lives
 * is lost and the right total is shown before the next roll.
 */
@Getter
@RequiredArgsConstructor
public enum DiceMistakes {
    SEARCH_AGAIN("DiceSearchAgain"),
    LIVES("DiceLives");

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * @return the choice of a key, looking again for an unknown one
     */
    public static DiceMistakes of(final String key) {
        for (final DiceMistakes mistakes : values()) {
            if (mistakes.key.equals(key)) {
                return mistakes;
            }
        }
        return SEARCH_AGAIN;
    }
}
