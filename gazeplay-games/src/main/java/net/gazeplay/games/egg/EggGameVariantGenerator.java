package net.gazeplay.games.egg;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

public class EggGameVariantGenerator extends IntRangeVariantGenerator {

    public EggGameVariantGenerator() {
        super("stepsToHatch", 2, 5);
    }

}
