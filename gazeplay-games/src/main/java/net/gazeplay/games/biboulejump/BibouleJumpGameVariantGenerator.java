package net.gazeplay.games.biboulejump;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "BibJump" proposed in the menu.
 */
public class BibouleJumpGameVariantGenerator extends EnumGameVariantGenerator<BibouleJumpVariant> {

    public BibouleJumpGameVariantGenerator() {
        super(BibouleJumpVariant.values(), BibouleJumpVariant::getLabel);
    }
}
