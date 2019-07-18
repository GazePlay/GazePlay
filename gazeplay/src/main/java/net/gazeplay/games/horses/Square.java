package net.gazeplay.games.horses;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.utils.Position;

public class Square {

    @Getter
    @Setter
    private Square nextSquare;
    @Getter
    @Setter
    private Square previousSquare;
    @Getter
    private Position pawnPosition;
    protected Pawn stationnedPawn;
    private Horses game;

    public Square(Position pawnPosition, Horses game) {
        this.pawnPosition = pawnPosition;
        this.game = game;
        stationnedPawn = null;
    }

    protected Square getNextSquare(Pawn pawn){
        return nextSquare;
    }

    public void pawnLands(Pawn pawn){
        if(stationnedPawn != null && stationnedPawn != pawn){
            stationnedPawn.moveBackToStart();
        }
        stationnedPawn = pawn;
        game.endOfTurn();
    }

    public Square getDestination(Pawn pawn, int nbMovementsLeft, int nbMovementsTotal) {
        if(nbMovementsLeft == nbMovementsTotal){
            stationnedPawn = null;
        }
        if(nbMovementsLeft > 0){
            return getNextSquare(pawn);
        }else {
            return previousSquare;
        }
    }

    public boolean canPawnMove(int diceOutcome) {
        return true;
    }

    public boolean isOccupied(){
        return stationnedPawn != null;
    }
}
