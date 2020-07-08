package net.gazeplay.games.goosegame;

import net.gazeplay.commons.gamevariants.generators.IntRangeVariantGenerator;

public class GooseGameVariantGenerator extends IntRangeVariantGenerator {

    public GooseGameVariantGenerator() {
        super("Choose number of players", 2, 5);
    }

}
