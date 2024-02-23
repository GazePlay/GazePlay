package net.gazeplay.games.rushhour;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class RushHourEmmanuelVariantGenerator extends EnumGameVariantGenerator<RushHourEmmanuelGameVariant> {

    public RushHourEmmanuelVariantGenerator(){
        super(RushHourEmmanuelGameVariant.values(), RushHourEmmanuelGameVariant::getLabel);
    }
}
