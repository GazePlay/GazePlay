package net.gazeplay.games.goosegame;


import net.gazeplay.components.Position;

/**
 * Last square of the Goose game: the pawn landing on it wins.
 */
public class EndSquare extends Square {
    public EndSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        super(number, pawnPosition, previousSquare, game);
    }

    @Override
    protected void pawnStays(Pawn pawn) {
        game.winner(pawn);
    }
}
