package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The number of bottles of a series of "Bouteille", on two shelves (the buttons of the variants).
 */
@Getter
@RequiredArgsConstructor
public enum BottleCount {
    FOUR("Bottles4", 4),
    EIGHT("Bottles8", 8),
    TWELVE("Bottles12", 12),
    SIXTEEN("Bottles16", 16);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    private final int bottles;

    /**
     * @return the count of a key, 8 for an unknown one
     */
    public static BottleCount of(final String key) {
        for (final BottleCount count : values()) {
            if (count.key.equals(key)) {
                return count;
            }
        }
        return EIGHT;
    }

    /**
     * @return the count of a number of bottles of before the review, the nearest one
     */
    public static BottleCount of(final int bottles) {
        for (final BottleCount count : values()) {
            if (count.bottles >= bottles) {
                return count;
            }
        }
        return SIXTEEN;
    }
}
