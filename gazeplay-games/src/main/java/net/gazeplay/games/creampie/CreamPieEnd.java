package net.gazeplay.games.creampie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Tartes à la crème" ends (maintainer's choice of 12/11/2026): after 10, 20 or 30 pies, or never.
 */
@Getter
@RequiredArgsConstructor
public enum CreamPieEnd {
    PIES_10("PieEnd10", 10),
    PIES_20("PieEnd20", 20),
    PIES_30("PieEnd30", 30),
    ENDLESS("PieEndless", 0);

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * The pies to throw, 0 without end.
     */
    private final int pies;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static CreamPieEnd of(final String key) {
        for (final CreamPieEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
