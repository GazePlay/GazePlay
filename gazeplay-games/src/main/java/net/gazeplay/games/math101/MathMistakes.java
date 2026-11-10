package net.gazeplay.games.math101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What a wrong answer does in "Apprends les Maths" (maintainer's choice of 10/11/2026): the card is greyed and the
 * player looks again until the right answer is found (no lives), or one of 3 lives is lost and the right answer is
 * shown before the next operation.
 */
@Getter
@RequiredArgsConstructor
public enum MathMistakes {
    SEARCH_AGAIN("MathSearchAgain"),
    LIVES("MathLives");

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
