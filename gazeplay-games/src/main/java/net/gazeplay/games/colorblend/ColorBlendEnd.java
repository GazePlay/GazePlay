package net.gazeplay.games.colorblend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the challenge of "Mélanger les couleurs" ends (maintainer's choice of 13/11/2026): after 5, 10 or 20 colors, or
 * never. The free mode has no end.
 */
@Getter
@RequiredArgsConstructor
public enum ColorBlendEnd {
    COLORS_5("ColorBlendEnd5", 5),
    COLORS_10("ColorBlendEnd10", 10),
    COLORS_20("ColorBlendEnd20", 20),
    ENDLESS("ColorBlendEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The colors to make, 0 without end.
     */
    private final int colors;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static ColorBlendEnd of(final String key) {
        for (final ColorBlendEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
