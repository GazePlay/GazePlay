package net.gazeplay.games.scratchcard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Cartes à gratter" ends (maintainer's choice of 12/11/2026): after 3, 5 or 10 cards uncovered, or never.
 */
@Getter
@RequiredArgsConstructor
public enum ScratchCardEnd {
    CARDS_3("ScratchEnd3", 3),
    CARDS_5("ScratchEnd5", 5),
    CARDS_10("ScratchEnd10", 10),
    ENDLESS("ScratchEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The cards to uncover, 0 without end.
     */
    private final int cards;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static ScratchCardEnd of(final String key) {
        for (final ScratchCardEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
