package net.gazeplay.games.cups;

import net.gazeplay.GameSpec;

public class CupsBallsGameVariantGenerator extends GameSpec.IntListVariantGenerator {

    public CupsBallsGameVariantGenerator() {
        super("Choose number of cups", 3, 5);
    }

}
