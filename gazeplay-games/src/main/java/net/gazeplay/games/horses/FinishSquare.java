package net.gazeplay.games.horses;


import net.gazeplay.components.Position;

/**
 * The last square of the final path: the pawn reaching it wins.
 */
public class FinishSquare extends Square {

    public FinishSquare(Position pawnPosition, Horses game) {
        super(pawnPosition, game);
    }

    @Override
    public void pawnLands(Pawn pawn) {
        game.win(pawn);
    }
}
