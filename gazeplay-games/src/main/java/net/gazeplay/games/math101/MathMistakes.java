package net.gazeplay.games.math101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.LivesVariant;

/**
 * What a wrong answer does in "Apprends les Maths" (maintainer's choice of 10/11/2026): one of 3 lives is lost and the
 * right answer is shown before the next operation, or the card is greyed and the player looks again until the right
 * answer is found (no lives). Since 17/11/2026, the switch is the common "with hearts / without hearts"
 * ({@link LivesVariant}, its keys and its order, with hearts first), in place of "3 vies / Chercher jusqu'à trouver".
 */
@Getter
@RequiredArgsConstructor
public enum MathMistakes {
    LIVES(LivesVariant.WITH),
    SEARCH_AGAIN(LivesVariant.WITHOUT);

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * @return the choice of a key, looking again for an unknown one
     */
    public static MathMistakes of(final String key) {
        for (final MathMistakes mistakes : values()) {
            if (mistakes.key.equals(key)) {
                return mistakes;
            }
        }
        return SEARCH_AGAIN;
    }
}
