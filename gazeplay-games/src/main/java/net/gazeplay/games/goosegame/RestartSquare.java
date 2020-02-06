package net.gazeplay.games.goosegame;


import net.gazeplay.components.Position;

public class RestartSquare extends Square {

    private final Square firstSquare;

    public RestartSquare(final int number, final Position pawnPosition, final Square previousSquare, final GooseGame game, final Square firstSquare) {
        super(number, pawnPosition, previousSquare, game);
        this.firstSquare = firstSquare;
    }

    @Override
    protected void pawnStays(final Pawn pawn) {
        pawn.moveToSquare(firstSquare);
        game.showMessage("Restart from the beginning");
    }
}
