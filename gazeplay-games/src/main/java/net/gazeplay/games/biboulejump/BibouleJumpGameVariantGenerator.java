package net.gazeplay.games.biboulejump;

import net.gazeplay.GameSpec;

public class BibouleJumpGameVariantGenerator extends GameSpec.EnumGameVariantGenerator<BibouleJumpVariant> {

    public BibouleJumpGameVariantGenerator() {
        super(BibouleJumpVariant.values(), BibouleJumpVariant::getLabel);
    }

}
