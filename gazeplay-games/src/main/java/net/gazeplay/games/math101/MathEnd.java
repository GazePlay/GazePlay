package net.gazeplay.games.math101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Apprends les Maths" ends (maintainer's choice of 10/11/2026): after 5, 10 or 20 operations solved, or never.
 */
@Getter
@RequiredArgsConstructor
public enum MathEnd {
    OPERATIONS_5("MathOperations5", 5),
    OPERATIONS_10("MathOperations10", 10),
    OPERATIONS_20("MathOperations20", 20),
    ENDLESS("MathEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The operations to solve, 0 without end.
     */
    private final int operations;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static MathEnd of(final String key) {
        for (final MathEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
