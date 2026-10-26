package net.gazeplay.games.space;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "SpaceGame" proposed in the menu.
 */
public class SpaceGameVariantGenerator extends EnumGameVariantGenerator<SpaceGameVariant> {

    public SpaceGameVariantGenerator() {
        super(SpaceGameVariant.values(), SpaceGameVariant::getLabel);
    }
}
