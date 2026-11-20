package net.gazeplay.games.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.LivesVariant;

/**
 * What a wrong total does in the counting mode of "Dés" (maintainer's choice of 12/11/2026, as in "Apprends les
 * Maths"): one of 3 lives is lost and the right total is shown before the next roll, or the card is greyed and the
 * player looks again until the right total is found (no lives). Since 17/11/2026, the switch is the common "with hearts
 * / without hearts" ({@link LivesVariant}, its keys and its order, without hearts first since 20/11/2026: the easiest
 * first, chosen by default), in place of "3 vies / Chercher jusqu'à trouver".
 */
@Getter
@RequiredArgsConstructor
public enum DiceMistakes {
    SEARCH_AGAIN(LivesVariant.WITHOUT),
    LIVES(LivesVariant.WITH);

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
