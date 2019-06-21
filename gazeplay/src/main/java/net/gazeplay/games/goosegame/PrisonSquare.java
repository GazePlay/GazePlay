package net.gazeplay.games.goosegame;

import net.gazeplay.commons.utils.Position;

public class PrisonSquare extends Square {

    private Pawn lockedPawn;

    public PrisonSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        super(number, pawnPosition, previousSquare, game);
    }

    @Override
    protected void pawnStays(Pawn pawn){
        game.endOfTurn();
        pawn.imprison();
        lockedPawn = pawn;
    }

    @Override
    protected void pawnPassesBy(Pawn pawn){
        if(lockedPawn != null){
            lockedPawn.free();
            lockedPawn = null;
        }
    }
}
