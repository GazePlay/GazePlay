package net.gazeplay.games.ninja;

import net.gazeplay.GameSpec;

public class NinjaGameVariantGenerator extends GameSpec.EnumGameVariantGenerator<NinjaGameVariant> {

    public NinjaGameVariantGenerator() {
        super(NinjaGameVariant.values(), NinjaGameVariant::getLabel);
    }

}
