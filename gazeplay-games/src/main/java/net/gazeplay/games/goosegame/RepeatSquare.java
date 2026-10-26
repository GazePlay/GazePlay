package net.gazeplay.games.goosegame;


import net.gazeplay.components.Position;

/**
 * Goose square: the pawn landing on it moves again by its last throw.
 */
public class RepeatSquare extends Square {
    public RepeatSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        super(number, pawnPosition, previousSquare, game);
    }

    @Override
    protected void pawnStays(Pawn pawn) {
        pawn.move(pawn.getLastThrowResult());
        game.showMessage("Repeat last throw!");
    }
}
