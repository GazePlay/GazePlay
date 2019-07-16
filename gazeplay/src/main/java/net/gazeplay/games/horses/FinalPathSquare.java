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
                return getNextSquare();
            }else{
                return null;
            }
        }else{
            pawn.invertMovement();
            return getPreviousSquare();
        }
    }
}
