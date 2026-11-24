package net.gazeplay.games.magiccards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * The cards to find in "Cartes magiques" (maintainer's decision of 24/11/2026, tester's feedback point 79: following
 * several cards is hard; one card is the easiest): 1, 2 or 3 among the cards, always fewer than the cards (3 cards: 1
 * or 2). In the order of the window of the variants, the easiest first, so chosen by default.
 */
@Getter
@RequiredArgsConstructor
public enum MagicCardsFind {
    ONE("MagicFind1", 1),
    TWO("MagicFind2", 2),
    THREE("MagicFind3", 3);

    /**
     * Key of translation, and switch of the variants.
     */
    private final String key;

    private final int cards;

    /**
     * @return whether this number of cards to find can be played with these cards: fewer than them
     */
    public boolean fits(final MagicCardsCount count) {
        return cards < count.getCards();
    }

    /**
     * @return the number of cards to find among the switches of a variant, null when none (a variant of before: half of
     *   the cards, {@link MagicCardsCount#getToFind()})
     */
    public static MagicCardsFind of(final List<String> switches) {
        for (final MagicCardsFind find : values()) {
            if (switches.contains(find.key)) {
                return find;
            }
        }
        return null;
    }
}
