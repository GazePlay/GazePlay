package net.gazeplay.games.space;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class SpaceGameVariantGenerator extends EnumGameVariantGenerator<SpaceGameVariant> {

    public SpaceGameVariantGenerator() {
        super(SpaceGameVariant.values(), SpaceGameVariant::getLabel);
    }
}
