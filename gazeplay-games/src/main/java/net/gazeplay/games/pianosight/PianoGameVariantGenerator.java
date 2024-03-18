package net.gazeplay.games.pianosight;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class PianoGameVariantGenerator extends EnumGameVariantGenerator<PianoGameVariant> {

    public PianoGameVariantGenerator() {
        super(PianoGameVariant.values(), PianoGameVariant::getLabel);
    }
    
}
