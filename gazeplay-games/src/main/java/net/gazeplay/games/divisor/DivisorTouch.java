package net.gazeplay.games.divisor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a target of "Diviseur" bursts (maintainer's choice of 12/11/2026, as in the shooting game and Ninja): as soon as
 * it is touched, or after a fixation of the length of the settings, which a ring shows.
 */
@Getter
@RequiredArgsConstructor
public enum DivisorTouch {
    AT_ONCE("DivisorAtOnce"),
    FIXATION("DivisorFixation");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at once for an unknown one
     */
    public static DivisorTouch of(final String key) {
        for (final DivisorTouch touch : values()) {
            if (touch.key.equals(key)) {
                return touch;
            }
        }
        return AT_ONCE;
    }
}
