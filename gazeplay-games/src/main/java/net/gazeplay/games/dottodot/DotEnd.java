package net.gazeplay.games.dottodot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Point par point" ends (maintainer's choice of 12/11/2026): after 3, 5 or 10 drawings, or never.
 */
@Getter
@RequiredArgsConstructor
public enum DotEnd {
    DRAWINGS_3("DotEnd3", 3),
    DRAWINGS_5("DotEnd5", 5),
    DRAWINGS_10("DotEnd10", 10),
    ENDLESS("DotEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The drawings to make, 0 without end.
     */
    private final int drawings;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static DotEnd of(final String key) {
        for (final DotEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
