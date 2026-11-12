package net.gazeplay.games.creampie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the pie is thrown in "Tartes à la crème" (maintainer's choice of 12/11/2026): after a fixation of the length of
 * the settings, as before, or as soon as the target is looked at, for the youngest.
 */
@Getter
@RequiredArgsConstructor
public enum CreamPieThrow {
    FIXATION("PieAfterFixation"),
    LOOK("PieAtOnce");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, after a fixation for an unknown one
     */
    public static CreamPieThrow of(final String key) {
        for (final CreamPieThrow way : values()) {
            if (way.key.equals(key)) {
                return way;
            }
        }
        return FIXATION;
    }
}
