package net.gazeplay.games.goosegame;


import net.gazeplay.components.Position;

public class PrisonSquare extends Square {

    private Pawn lockedPawn;
    private final String message;

    public PrisonSquare(final int number, final Position pawnPosition, final Square previousSquare, final GooseGame game, final String message) {
        super(number, pawnPosition, previousSquare, game);
        this.message = message;
    }

    @Override
    protected void pawnStays(final Pawn pawn) {
        game.showMessage(message, pawn.getNumber());
        pawn.imprison();
        lockedPawn = pawn;
        game.endOfTurn();
    }

    @Override
    protected void pawnPassesBy(final Pawn pawn) {
        if (lockedPawn != null) {
            lockedPawn.free();
            game.showMessage("Player %d freed player %d", pawn.getNumber(), lockedPawn.getNumber());
            lockedPawn = null;
        }
        game.playMovementSound();
    }
}
