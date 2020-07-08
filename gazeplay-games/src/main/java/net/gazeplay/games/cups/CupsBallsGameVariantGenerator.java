package net.gazeplay.games.cups;

import net.gazeplay.commons.gamevariants.generators.IntListVariantGenerator;

public class CupsBallsGameVariantGenerator extends IntListVariantGenerator {

    public CupsBallsGameVariantGenerator() {
        super("Choose number of cups", 3, 5);
    }

}
