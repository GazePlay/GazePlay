package net.gazeplay.games.magicpotions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Magic Potions" ends (maintainer's choice of 10/11/2026): after 3, 5 or 10 potions served, or never (until the
 * lives are lost).
 */
@Getter
@RequiredArgsConstructor
public enum PotionsEnd {
    SERVED_3("PotionsServed3", 3),
    SERVED_5("PotionsServed5", 5),
    SERVED_10("PotionsServed10", 10),
    ENDLESS("PotionsEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The potions to serve, 0 without end.
     */
    private final int served;

    /**
     * @return the end of a key, 5 potions for an unknown one
     */
    public static PotionsEnd of(final String key) {
        for (final PotionsEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return SERVED_5;
    }
}
