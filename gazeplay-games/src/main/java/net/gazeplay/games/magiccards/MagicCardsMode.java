package net.gazeplay.games.magiccards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Cartes magiques" is played (maintainer's choices of 12/11/2026): the magic trick: the pictures are shown on
 * their cards, the cards turn over and are shuffled before the eyes, then the cards are to be found again. The search
 * (a picture hidden under one of the cards, found by chance) was removed on 17/11/2026 (maintainer's decision): one
 * mode only, kept as the group of the variants (a single value: the window shows no switch for it).
 */
@Getter
@RequiredArgsConstructor
public enum MagicCardsMode {
    MAGIC("MagicShuffle");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key: the magic, the only one (a variant of the search of before is played so)
     */
    public static MagicCardsMode of(final String key) {
        return MAGIC;
    }
}
