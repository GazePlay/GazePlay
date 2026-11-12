package net.gazeplay.games.magiccards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Cartes magiques" is played (maintainer's choices of 12/11/2026): look for the picture hidden under one of the
 * cards, or the magic trick: the picture is shown on a card, the cards turn over and are shuffled before the eyes,
 * then the card is to be found again.
 */
@Getter
@RequiredArgsConstructor
public enum MagicCardsMode {
    SEARCH("MagicSearch"),
    MAGIC("MagicShuffle");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the search for an unknown one
     */
    public static MagicCardsMode of(final String key) {
        for (final MagicCardsMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return SEARCH;
    }
}
