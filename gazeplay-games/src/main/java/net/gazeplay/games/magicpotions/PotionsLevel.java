package net.gazeplay.games.magicpotions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Magic Potions" (maintainer's choices of 10/11/2026): with the recipe (the potions to pour shown under
 * the request), or without it.
 */
@Getter
@RequiredArgsConstructor
public enum PotionsLevel {
    WITH_RECIPE("PotionsWithRecipe"),
    WITHOUT_RECIPE("PotionsWithoutRecipe");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the level of a key, with the recipe for an unknown one
     */
    public static PotionsLevel of(final String key) {
        for (final PotionsLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return WITH_RECIPE;
    }
}
