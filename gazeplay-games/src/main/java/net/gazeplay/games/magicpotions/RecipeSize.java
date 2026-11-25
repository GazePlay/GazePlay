package net.gazeplay.games.magicpotions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The size of the recipes of "Recettes magiques" (maintainer's decision of 24/11/2026, tester's feedback point 82): 2, 3
 * or 4 ingredients, the main choice of the variants; the shelf shows 2 more ingredients than the recipe.
 */
@Getter
@RequiredArgsConstructor
public enum RecipeSize {
    TWO("RecipeIngredients2", 2),
    THREE("RecipeIngredients3", 3),
    FOUR("RecipeIngredients4", 4);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The ingredients of a recipe.
     */
    private final int ingredients;

    /**
     * @return the ingredients on the shelf: those of the recipe and 2 others
     */
    public int getShelf() {
        return ingredients + 2;
    }

    /**
     * @return the size of a key, 2 ingredients for an unknown one
     */
    public static RecipeSize of(final String key) {
        for (final RecipeSize size : values()) {
            if (size.key.equals(key)) {
                return size;
            }
        }
        return TWO;
    }
}
