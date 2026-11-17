package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Bouteille" is played (maintainer's choices of 13/11/2026): break only the bottles of the color asked. The mode
 * "break them all" was removed on 17/11/2026 (maintainer's decision): one mode only, kept as the group of the variants
 * (a single value: the window shows no switch for it).
 */
@Getter
@RequiredArgsConstructor
public enum BottleMode {
    COLOR("BottleColorMode");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key: the right color, the only one (a variant of "break them all" of before is played so)
     */
    public static BottleMode of(final String key) {
        return COLOR;
    }
}
