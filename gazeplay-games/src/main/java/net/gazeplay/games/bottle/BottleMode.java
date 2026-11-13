package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Bouteille" is played (maintainer's choices of 13/11/2026): break all the bottles, or only those of the color
 * asked.
 */
@Getter
@RequiredArgsConstructor
public enum BottleMode {
    ALL("BottleBreakAll"),
    COLOR("BottleColorMode");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, all the bottles for an unknown one
     */
    public static BottleMode of(final String key) {
        for (final BottleMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return ALL;
    }
}
