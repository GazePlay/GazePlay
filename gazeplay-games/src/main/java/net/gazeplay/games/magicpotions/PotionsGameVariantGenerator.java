package net.gazeplay.games.magicpotions;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Recettes magiques" (maintainer's choices of 24 and 25/11/2026): at the top, the recipe visible or
 * hidden after a few seconds, the ingredients of a recipe (buttons, see {@link RecipeSize}: 2, 3 or 4), and at the bottom
 * when it ends (see {@link PotionsEnd}: 3, 5 or 10 recipes, or without end) and the hearts (see {@link LivesVariant}).
 * The easiest first, chosen when the window opens.
 */
public class PotionsGameVariantGenerator implements IGameVariantGenerator {

    static final String VISIBLE = "PotionsRecipeVisible";
    static final String HIDDEN = "PotionsRecipeHidden";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final String recipe : new String[]{VISIBLE, HIDDEN}) {
            for (final PotionsEnd end : PotionsEnd.values()) {
                for (final String lives : LivesVariant.options(true)) {
                    for (final RecipeSize size : RecipeSize.values()) {
                        variants.add(new GroupedGameVariant(recipe, end.getKey(), size.getKey(), null, lives));
                    }
                }
            }
        }
        return variants;
    }
}
