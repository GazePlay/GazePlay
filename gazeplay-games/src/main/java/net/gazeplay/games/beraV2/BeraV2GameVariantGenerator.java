package net.gazeplay.games.beraV2;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class BeraV2GameVariantGenerator extends EnumGameVariantGenerator<BeraV2GameVariant>{

    public BeraV2GameVariantGenerator(){
        super(BeraV2GameVariant.values(), BeraV2GameVariant::getLabel);
    }
}
