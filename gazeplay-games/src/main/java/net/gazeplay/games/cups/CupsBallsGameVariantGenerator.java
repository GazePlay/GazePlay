package net.gazeplay.games.cups;

import net.gazeplay.GameSpec;

public class CupsBallsGameVariantGenerator extends GameSpec.IntListVariantGenerator {

    private static final String CUPS = "cups";

    public CupsBallsGameVariantGenerator() {
        super(CUPS, 3, 5);
    }

}
