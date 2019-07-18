package net.gazeplay.games.horses;

import net.gazeplay.commons.utils.Position;

public class FinalPathSquare extends Square {

    private final int requiredNumber;

    public FinalPathSquare(Position pawnPosition, Horses game, int requiredNumber) {
        super(pawnPosition, game);
        this.requiredNumber = requiredNumber;
    }

    @Override
    public Square getDestination(Pawn pawn, int nbMovementsLeft, int nbMovementsTotal) {
        if(nbMovementsLeft == nbMovementsTotal){
            pawn.cancelMovement();
            if(nbMovementsTotal == requiredNumber){
                stationnedPawn = null;
                return getNextSquare();
            }else{
                return null;
            }
        }else{
            return getPreviousSquare();
        }
    }

    @Override
    public boolean canPawnMove(int diceOutcome) {
        return diceOutcome == requiredNumber && !getNextSquare().isOccupied();
    }
}
