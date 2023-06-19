package net.gazeplay.games.simon;
import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class SimonGameVariantGenerator extends EnumGameVariantGenerator<SimonGameVariant> {

    public SimonGameVariantGenerator(){
        super(SimonGameVariant.values(), SimonGameVariant::getLabel);
    }
}
