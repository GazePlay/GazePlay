package net.gazeplay.games.draw;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Gribouille" colors the line (maintainer's choice of 12/11/2026): with the color chosen in the palette of the
 * tool bar, or in rainbow colors changing by themselves along the line (the game before its review changed the color
 * each time the gaze came back on the sheet).
 */
@Getter
@RequiredArgsConstructor
public enum ScribbleMode {
    PALETTE("ScribblePalette"),
    RAINBOW("ScribbleRainbow");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the palette for an unknown one
     */
    public static ScribbleMode of(final String key) {
        for (final ScribbleMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return PALETTE;
    }
}
