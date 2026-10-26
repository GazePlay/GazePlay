package net.gazeplay.games.connect4;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Connect4" proposed in the menu.
 */
public class Connect4GameVariantGenerator extends EnumGameVariantGenerator<Connect4GameVariant> {

    public Connect4GameVariantGenerator() {
        super(Connect4GameVariant.values(), Connect4GameVariant::getLabel);
    }

}
