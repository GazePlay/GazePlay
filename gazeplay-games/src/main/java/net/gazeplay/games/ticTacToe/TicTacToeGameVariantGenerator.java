package net.gazeplay.games.ticTacToe;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "TicTacToe" proposed in the menu.
 */
public class TicTacToeGameVariantGenerator extends EnumGameVariantGenerator<TicTacToeGameVariant> {

    public TicTacToeGameVariantGenerator() {
        super(TicTacToeGameVariant.values(), TicTacToeGameVariant::getLabel);
    }
}
