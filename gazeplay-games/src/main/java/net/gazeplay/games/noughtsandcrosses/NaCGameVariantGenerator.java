package net.gazeplay.games.noughtsandcrosses;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class NaCGameVariantGenerator extends EnumGameVariantGenerator<NaCGameVariant> {

    public NaCGameVariantGenerator() {
        super(NaCGameVariant.values(), NaCGameVariant::getLabel);
    }
}
