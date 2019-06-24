package net.gazeplay.games.goosegame;

import net.gazeplay.commons.utils.Position;

public class PrisonSquare extends Square {

    private Pawn lockedPawn;

    public PrisonSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        super(number, pawnPosition, previousSquare, game);
    }

    @Override
    protected void pawnStays(Pawn pawn){
        game.showMessage("Player " + pawn.getNumber() + " fell into a well");
        pawn.imprison();
        lockedPawn = pawn;
        game.endOfTurn();
    }

    @Override
    protected void pawnPassesBy(Pawn pawn){
        if(lockedPawn != null){
            lockedPawn.free();
            game.showMessage("Player " + pawn.getNumber() + " freed player " + lockedPawn.getNumber());
            lockedPawn = null;
        }
    }
}
