package net.gazeplay.games.colors;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Coloring" proposed in the menu: the three levels of coloring pages (small, medium and
 * large palette).
 */
public class ColorsGameVariantGenerator extends EnumGameVariantGenerator<ColoringVariant> {

    public ColorsGameVariantGenerator() {
        super(ColoringVariant.values(), ColoringVariant::getLabel);
    }
}
