package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the fruit of "Ninja" is cut (maintainer's choice of 4/11/2026, as in the shooting game): as soon as it is touched,
 * or after a fixation of the length of the settings, which a ring shows.
 */
@Getter
@RequiredArgsConstructor
public enum NinjaCut {
    AT_ONCE("NinjaAtOnce"),
    FIXATION("NinjaFixation");

    /**
     * Key of translation, and first switch of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at once for an unknown one
     */
    public static NinjaCut of(final String key) {
        for (final NinjaCut cut : values()) {
            if (cut.key.equals(key)) {
                return cut;
            }
        }
        return AT_ONCE;
    }
}
