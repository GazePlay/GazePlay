package net.gazeplay.games.ninja;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class NinjaGameVariantGenerator extends EnumGameVariantGenerator<NinjaGameVariant> {

    public NinjaGameVariantGenerator() {
        super(NinjaGameVariant.values(), NinjaGameVariant::getLabel);
    }
}
