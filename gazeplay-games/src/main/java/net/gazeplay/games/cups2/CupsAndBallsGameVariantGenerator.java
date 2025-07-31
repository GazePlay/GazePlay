package net.gazeplay.games.cups2;

import net.gazeplay.commons.gamevariants.generators.IntListVariantGenerator;

public class CupsAndBallsGameVariantGenerator extends IntListVariantGenerator {

    public CupsAndBallsGameVariantGenerator() {
        super("Choose starting number of cups", 3, 5, 7);
    }

}
