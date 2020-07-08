package net.gazeplay.games.horses;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

public class HorsesGameVariantGenerator extends IntRangeVariantGenerator {

    public HorsesGameVariantGenerator() {
        super("Choose number of players", 2, 4);
    }

}
