package net.gazeplay.games.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Ordre" ends (maintainer's choice of 10/11/2026): after 3, 5 or 10 rounds won, or never (until the lives are
 * lost).
 */
@Getter
@RequiredArgsConstructor
public enum OrderEnd {
    ROUNDS_3("OrderRounds3", 3),
    ROUNDS_5("OrderRounds5", 5),
    ROUNDS_10("OrderRounds10", 10),
    ENDLESS("OrderEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The rounds to win, 0 without end.
     */
    private final int rounds;

    /**
     * @return the end of a key, 5 rounds for an unknown one
     */
    public static OrderEnd of(final String key) {
        for (final OrderEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ROUNDS_5;
    }
}
