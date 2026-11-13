package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Bouteille" ends (maintainer's choice of 13/11/2026): after 1, 3 or 5 series, or never.
 */
@Getter
@RequiredArgsConstructor
public enum BottleEnd {
    SERIES_1("BottleEnd1", 1),
    SERIES_3("BottleEnd3", 3),
    SERIES_5("BottleEnd5", 5),
    ENDLESS("BottleEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The series to play, 0 without end.
     */
    private final int series;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static BottleEnd of(final String key) {
        for (final BottleEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
