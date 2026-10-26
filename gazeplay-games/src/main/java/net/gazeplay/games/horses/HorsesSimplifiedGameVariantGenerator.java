package net.gazeplay.games.horses;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

/**
 * Gives the variants of the game "Horses Simplified" proposed in the menu.
 */
public class HorsesSimplifiedGameVariantGenerator extends IntRangeVariantGenerator {

    public HorsesSimplifiedGameVariantGenerator() {
        super("Choose number of players", 2, 4);
    }

}
