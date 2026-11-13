package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "La Fabrique à gâteaux" is played (maintainer's choices of 12/11/2026): copy a model drawn at random, as before,
 * or make one's own cake (new).
 */
@Getter
@RequiredArgsConstructor
public enum CakesMode {
    COPY("CakesCopy"),
    FREE("CakesFree");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the copy for an unknown one
     */
    public static CakesMode of(final String key) {
        for (final CakesMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return COPY;
    }
}
