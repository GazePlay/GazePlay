package net.gazeplay.games.magiccards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Cartes magiques" ends (maintainer's choice of 12/11/2026): after 3, 5 or 10 pictures found, or never.
 */
@Getter
@RequiredArgsConstructor
public enum MagicCardsEnd {
    PICTURES_3("MagicEnd3", 3),
    PICTURES_5("MagicEnd5", 5),
    PICTURES_10("MagicEnd10", 10),
    ENDLESS("MagicEndless", 0);

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * The pictures to find, 0 without end.
     */
    private final int pictures;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static MagicCardsEnd of(final String key) {
        for (final MagicCardsEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
