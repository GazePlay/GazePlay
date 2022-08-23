package net.gazeplay.games.bera;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class BeraGameVariantGenerator extends EnumGameVariantGenerator<BeraGameVariant> {

    public BeraGameVariantGenerator() {
        super(BeraGameVariant.values(), BeraGameVariant::getLabel);
    }
}
