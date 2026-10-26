package net.gazeplay.games.ninja;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Ninja" proposed in the menu.
 */
public class NinjaGameVariantGenerator extends EnumGameVariantGenerator<NinjaGameVariant> {

    public NinjaGameVariantGenerator() {
        super(NinjaGameVariant.values(), NinjaGameVariant::getLabel);
    }
}
