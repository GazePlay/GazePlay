package net.gazeplay.games.moles;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "WhacAMole" proposed in the menu.
 */
public class MolesGameVariantGenerator extends EnumGameVariantGenerator<MolesGameVariant> {

    public MolesGameVariantGenerator() {
        super(MolesGameVariant.values(), MolesGameVariant::getLabel);
    }
}
