package net.gazeplay.games.goosegame;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

/**
 * Gives the variants of the game "GooseGame" proposed in the menu.
 */
public class GooseGameVariantGenerator extends IntRangeVariantGenerator {

    public GooseGameVariantGenerator() {
        super("Choose number of players", 2, 5);
    }

}
