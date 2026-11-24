package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Bouteille" is played: break all the bottles, or only those of the color asked. "Break them all", removed on
 * 17/11/2026, came back on 24/11/2026 (maintainer's decision, tester's feedback point 78: it shows that the gaze makes
 * something happen, without any rule), first as the easier one; the right color is named "Détruire les cibles".
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
     * @return the mode of a key, the right color for an unknown one (the variants of before the review are read so)
     */
    public static BottleMode of(final String key) {
        for (final BottleMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return COLOR;
    }
}
