package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "La Fabrique à gâteaux" ends (maintainer's choice of 12/11/2026): after 1, 3 or 5 cakes, or never.
 */
@Getter
@RequiredArgsConstructor
public enum CakesEnd {
    CAKES_1("CakesEnd1", 1),
    CAKES_3("CakesEnd3", 3),
    CAKES_5("CakesEnd5", 5),
    ENDLESS("CakesEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The cakes to make, 0 without end.
     */
    private final int cakes;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static CakesEnd of(final String key) {
        for (final CakesEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
