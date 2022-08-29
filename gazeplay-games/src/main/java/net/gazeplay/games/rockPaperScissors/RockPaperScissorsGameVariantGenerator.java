package net.gazeplay.games.rockPaperScissors;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class RockPaperScissorsGameVariantGenerator extends EnumGameVariantGenerator<RockPaperScissorsGameVariant> {
    public RockPaperScissorsGameVariantGenerator() {
        super(RockPaperScissorsGameVariant.values(), RockPaperScissorsGameVariant::getLabel);
    }
}
