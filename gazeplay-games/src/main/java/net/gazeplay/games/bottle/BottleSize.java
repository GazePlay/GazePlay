package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The size of the bottles of "Bouteille" (maintainer's choice of 13/11/2026, a switch of the variants): their height in
 * the space between two shelves; they always keep their shape, narrower if their place on the shelf is too small.
 */
@Getter
@RequiredArgsConstructor
public enum BottleSize {
    SMALL("BottleSmall", 0.45),
    MEDIUM("BottleMedium", 0.62),
    LARGE("BottleLarge", 0.8);

    /**
     * Key of translation, and value of the switch.
     */
    private final String key;

    /**
     * The height of a bottle, as a part of the space between two shelves.
     */
    private final double height;

    /**
     * @return the size of a key, medium for an unknown one
     */
    public static BottleSize of(final String key) {
        for (final BottleSize size : values()) {
            if (size.key.equals(key)) {
                return size;
            }
        }
        return MEDIUM;
    }
}
