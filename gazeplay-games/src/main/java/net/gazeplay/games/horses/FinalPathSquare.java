package net.gazeplay.games.horses;


import net.gazeplay.components.Position;

/**
 * A square of the final path of a team ("Horses"): the pawn goes up to the next square only with the exact number shown
 * on the square.
 */
public class FinalPathSquare extends Square {

    private final int requiredNumber;

    public FinalPathSquare(Position pawnPosition, Horses game, int requiredNumber) {
        super(pawnPosition, game);
        this.requiredNumber = requiredNumber;
    }

    @Override
    public Square getDestination(Pawn pawn, int nbMovementsLeft, int nbMovementsTotal) {
        if (nbMovementsLeft == nbMovementsTotal) {
            pawn.cancelMovement();
            if (nbMovementsTotal == requiredNumber) {
                stationnedPawn = null;
                return getNextSquare();
            } else {
                return null;
            }
        } else {
            return getPreviousSquare();
        }
    }

    @Override
    public Square peekDestination(Pawn pawn, int nbMovementsLeft, int nbMovementsTotal, Square departure) {
        if (nbMovementsLeft == nbMovementsTotal) {
            return nbMovementsTotal == requiredNumber ? getNextSquare() : null;
        }
        return getPreviousSquare();
    }

    @Override
    public boolean canPawnMove(int diceOutcome) {
        return diceOutcome == requiredNumber && !getNextSquare().isOccupied();
    }
}
