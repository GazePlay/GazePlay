package net.gazeplay.games.ticTacToe;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class TicTacToeGameVariantGenerator extends EnumGameVariantGenerator<TicTacToeGameVariant> {

    public TicTacToeGameVariantGenerator() {
        super(TicTacToeGameVariant.values(), TicTacToeGameVariant::getLabel);
    }
}
