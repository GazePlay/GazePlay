package net.gazeplay.games.goosegame;

import lombok.Setter;
import net.gazeplay.commons.utils.Position;

public class BridgeSquare extends Square {
    @Setter
    Square destinationSquare;

    public BridgeSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        super(number, pawnPosition, previousSquare, game);
    }

    @Override
    protected void pawnStays(Pawn pawn){
        pawn.moveToSquare(destinationSquare);
    }
}
