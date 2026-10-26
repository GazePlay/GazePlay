package net.gazeplay.games.dottodot;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "DotToDot" proposed in the menu.
 */
public class DotToDotGameVariantGenerator extends EnumGameVariantGenerator<DotToDotGameVariant> {
    public DotToDotGameVariantGenerator() {
        super(DotToDotGameVariant.values(), DotToDotGameVariant::getLabel);
    }
}
