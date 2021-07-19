package net.gazeplay.games.moles;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class MolesGameVariantGenerator extends EnumGameVariantGenerator<MolesGameVariant> {

    public MolesGameVariantGenerator() {
        super(MolesGameVariant.values(), MolesGameVariant::getLabel);
    }
}
