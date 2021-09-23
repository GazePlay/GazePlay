package net.gazeplay.games.dottodot;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class DotToDotGameVariantGenerator extends EnumGameVariantGenerator<DotToDotGameVariant> {
    public DotToDotGameVariantGenerator() {
        super(DotToDotGameVariant.values(), DotToDotGameVariant::getLabel);
    }
}
