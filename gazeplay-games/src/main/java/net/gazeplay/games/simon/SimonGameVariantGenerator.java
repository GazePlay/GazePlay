package net.gazeplay.games.simon;
import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Simon" proposed in the menu.
 */
public class SimonGameVariantGenerator extends EnumGameVariantGenerator<SimonGameVariant> {

    public SimonGameVariantGenerator(){
        super(SimonGameVariant.values(), SimonGameVariant::getLabel);
    }
}
