package net.gazeplay.games.blocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Blocs" ends (maintainer's choice of 13/11/2026): after 3, 5 or 10 pictures, or never.
 */
@Getter
@RequiredArgsConstructor
public enum BlocsEnd {
    PICTURES_3("BlocsEnd3", 3),
    PICTURES_5("BlocsEnd5", 5),
    PICTURES_10("BlocsEnd10", 10),
    ENDLESS("BlocsEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The pictures to uncover, 0 without end.
     */
    private final int pictures;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static BlocsEnd of(final String key) {
        for (final BlocsEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
