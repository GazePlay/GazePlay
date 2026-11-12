package net.gazeplay.games.magiccards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The number of cards of "Cartes magiques" (maintainer's choice of 12/11/2026), and their grid: 3 in a row (new, for the
 * youngest), 4, 6 or 9.
 */
@Getter
@RequiredArgsConstructor
public enum MagicCardsCount {
    THREE("MagicCards3", 1, 3, 4),
    FOUR("MagicCards4", 2, 2, 5),
    SIX("MagicCards6", 2, 3, 6),
    NINE("MagicCards9", 3, 3, 7);

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    private final int rows;

    private final int columns;

    /**
     * The swaps of two cards in the magic mode.
     */
    private final int swaps;

    public int getCards() {
        return rows * columns;
    }

    /**
     * @return the number of cards of a key, 4 for an unknown one
     */
    public static MagicCardsCount of(final String key) {
        for (final MagicCardsCount count : values()) {
            if (count.key.equals(key)) {
                return count;
            }
        }
        return FOUR;
    }

    /**
     * @return the number of cards of a grid of before the review (2 x 2, 2 x 3, 3 x 2, 3 x 3), the nearest one
     */
    public static MagicCardsCount ofCards(final int cards) {
        MagicCardsCount nearest = FOUR;
        for (final MagicCardsCount count : values()) {
            if (Math.abs(count.getCards() - cards) < Math.abs(nearest.getCards() - cards)) {
                nearest = count;
            }
        }
        return nearest;
    }
}
