package net.gazeplay.games.bubbles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Where the bubbles of "Bubbles" go: up, down, to the right, to the left, or nowhere (they stay still). Its name is
 * kept: the variants saved before the review of 2/11/2026 used it ({@code EnumGameVariant}).
 */
@Getter
@RequiredArgsConstructor
public enum BubblesGameVariant {
    TOP("ToTop"),
    BOTTOM("ToBottom"),
    RIGHT("ToRight"),
    LEFT("ToLeft"),
    FIX("Fix");

    /**
     * Key of translation, and value of the variants.
     */
    private final String direction;

    /**
     * @return the direction of a key, up for an unknown one
     */
    public static BubblesGameVariant of(final String key) {
        for (final BubblesGameVariant direction : values()) {
            if (direction.direction.equals(key)) {
                return direction;
            }
        }
        return TOP;
    }
}
