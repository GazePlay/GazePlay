package net.gazeplay.games.rockPaperScissors;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "RockPaperScissors" proposed in the menu.
 */
public class RockPaperScissorsGameVariantGenerator extends EnumGameVariantGenerator<RockPaperScissorsGameVariant> {
    public RockPaperScissorsGameVariantGenerator() {
        super(RockPaperScissorsGameVariant.values(), RockPaperScissorsGameVariant::getLabel);
    }
}
