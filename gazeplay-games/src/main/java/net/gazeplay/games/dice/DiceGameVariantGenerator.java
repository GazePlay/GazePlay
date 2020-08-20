package net.gazeplay.games.dice;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

public class DiceGameVariantGenerator extends IntRangeVariantGenerator {

    public DiceGameVariantGenerator() {
        super("Choose number of dices", 1, 6);
    }

}
