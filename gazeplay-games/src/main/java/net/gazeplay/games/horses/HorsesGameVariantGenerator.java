package net.gazeplay.games.horses;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

/**
 * Gives the variants of the game "Horses" proposed in the menu.
 */
public class HorsesGameVariantGenerator extends IntRangeVariantGenerator {

    public HorsesGameVariantGenerator() {
        super("Choose number of players", 2, 4);
    }

}
