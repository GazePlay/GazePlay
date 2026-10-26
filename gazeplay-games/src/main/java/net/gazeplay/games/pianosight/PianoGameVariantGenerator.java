package net.gazeplay.games.pianosight;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Piano" proposed in the menu.
 */
public class PianoGameVariantGenerator extends EnumGameVariantGenerator<PianoGameVariant> {

    public PianoGameVariantGenerator() {
        super(PianoGameVariant.values(), PianoGameVariant::getLabel);
    }
    
}
