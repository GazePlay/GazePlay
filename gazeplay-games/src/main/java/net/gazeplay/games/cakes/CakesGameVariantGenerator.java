package net.gazeplay.games.cakes;

import net.gazeplay.GameSpec;

public class CakesGameVariantGenerator extends GameSpec.EnumGameVariantGenerator<CakeGameVariant> {
    public CakesGameVariantGenerator() {
        super(CakeGameVariant.values(), CakeGameVariant::getLabel);
    }
}
