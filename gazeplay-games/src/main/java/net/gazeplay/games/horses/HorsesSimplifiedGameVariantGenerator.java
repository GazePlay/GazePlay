package net.gazeplay.games.horses;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

public class HorsesSimplifiedGameVariantGenerator extends IntRangeVariantGenerator {

    public HorsesSimplifiedGameVariantGenerator() {
        super("Choose number of players", 2, 4);
    }

}
