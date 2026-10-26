package net.gazeplay.games.soundsoflife;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "SoundsOfLife" proposed in the menu.
 */
public class SoundsOfLifeGameVariantGenerator extends EnumGameVariantGenerator<SoundsOfLifeGameVariant> {

    public SoundsOfLifeGameVariantGenerator() {
        super(SoundsOfLifeGameVariant.values(), SoundsOfLifeGameVariant::getLabel);
    }
}
