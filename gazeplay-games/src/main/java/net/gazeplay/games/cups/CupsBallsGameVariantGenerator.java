package net.gazeplay.games.cups;

import net.gazeplay.commons.gamevariants.generators.IntListVariantGenerator;

/**
 * Gives the variants of the game "CupsBalls" proposed in the menu.
 */
public class CupsBallsGameVariantGenerator extends IntListVariantGenerator {

    public CupsBallsGameVariantGenerator() {
        super("Choose number of cups", 3, 5);
    }

}
