package net.gazeplay.games.oddshape;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class OddShapeVariantGenerator extends EnumGameVariantGenerator<OddShapeVariant> {
    public OddShapeVariantGenerator() {
        super(OddShapeVariant.values(), OddShapeVariant::getLabel);
    }
}
