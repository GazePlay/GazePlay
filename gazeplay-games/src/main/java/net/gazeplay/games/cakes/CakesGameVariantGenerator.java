package net.gazeplay.games.cakes;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class CakesGameVariantGenerator extends EnumGameVariantGenerator<CakeGameVariant> {

    public CakesGameVariantGenerator() {
        super(CakeGameVariant.values(), CakeGameVariant::getLabel);
    }
}
