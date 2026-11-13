package net.gazeplay.games.colorblend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Mélanger les couleurs" is played (maintainer's choices of 13/11/2026): a color to make (the challenge), or one's
 * own mixes (the free mode).
 */
@Getter
@RequiredArgsConstructor
public enum ColorBlendMode {
    CHALLENGE("ColorBlendChallenge"),
    FREE("ColorBlendFree");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the free mode for an unknown one (the game had no variant before its review)
     */
    public static ColorBlendMode of(final String key) {
        for (final ColorBlendMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return FREE;
    }
}
