package net.gazeplay.games.cakes;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Cakes" proposed in the menu.
 */
public class CakesGameVariantGenerator extends EnumGameVariantGenerator<CakeGameVariant> {

    public CakesGameVariantGenerator() {
        super(CakeGameVariant.values(), CakeGameVariant::getLabel);
    }
}
