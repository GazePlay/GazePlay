package net.gazeplay.games.dice;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

/**
 * Gives the variants of the game "Dice" proposed in the menu.
 */
public class DiceGameVariantGenerator extends IntRangeVariantGenerator {

    public DiceGameVariantGenerator() {
        super("Choose number of dices", 1, 6);
    }

}
