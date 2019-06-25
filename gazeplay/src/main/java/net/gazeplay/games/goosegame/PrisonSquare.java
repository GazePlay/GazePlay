package net.gazeplay.games.goosegame;

import net.gazeplay.commons.utils.Position;

public class PrisonSquare extends Square {

    private Pawn lockedPawn;
    private String message;

    public PrisonSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game, String message) {
        super(number, pawnPosition, previousSquare, game);
        this.message = message;
    }

    @Override
    protected void pawnStays(Pawn pawn) {
        game.showMessage(message, pawn.getNumber());
        pawn.imprison();
        lockedPawn = pawn;
        game.endOfTurn();
    }

    @Override
    protected void pawnPassesBy(Pawn pawn) {
        if (lockedPawn != null) {
            lockedPawn.free();
            game.showMessage("Player %d freed player %d", pawn.getNumber(), lockedPawn.getNumber());
            lockedPawn = null;
        }
    }
}
