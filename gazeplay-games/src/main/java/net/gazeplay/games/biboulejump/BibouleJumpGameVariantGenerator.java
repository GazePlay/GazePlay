package net.gazeplay.games.biboulejump;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class BibouleJumpGameVariantGenerator extends EnumGameVariantGenerator<BibouleJumpVariant> {

    public BibouleJumpGameVariantGenerator() {
        super(BibouleJumpVariant.values(), BibouleJumpVariant::getLabel);
    }
}
