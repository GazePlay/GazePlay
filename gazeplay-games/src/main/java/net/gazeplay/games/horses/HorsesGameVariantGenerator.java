package net.gazeplay.games.horses;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Horses" proposed in the menu, on the normal and the simplified board: one player
 * against the computer, or several players (see {@link HorsesVariant}).
 */
public class HorsesGameVariantGenerator extends EnumGameVariantGenerator<HorsesVariant> {

    public HorsesGameVariantGenerator() {
        super(HorsesVariant.values(), HorsesVariant::getLabel);
    }
}
