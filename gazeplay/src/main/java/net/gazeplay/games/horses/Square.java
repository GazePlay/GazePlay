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
    protected Horses game;

    public Square(Position pawnPosition, Horses game) {
        this.pawnPosition = pawnPosition;
        this.game = game;
        stationnedPawn = null;
    }

    protected Square getNextSquare(Pawn pawn) {
        return nextSquare;
    }

    protected Square getPreviousSquare(Pawn pawn) {
        return previousSquare;
    }

    public void pawnLands(Pawn pawn) {
        if (stationnedPawn != null && stationnedPawn != pawn && stationnedPawn.getTeam() != pawn.getTeam()) {
            stationnedPawn.moveBackToStart();
        } else if (stationnedPawn != null) {
            pawn.moveToSquare(getPreviousSquare());
        }
        stationnedPawn = pawn;
        game.endOfTurn();
    }

    public Square getDestination(Pawn pawn, int nbMovementsLeft, int nbMovementsTotal) {
        if (nbMovementsLeft == nbMovementsTotal) {
            stationnedPawn = null;
        }
        if ((nbMovementsLeft > 0 && stationnedPawn == null) || (nbMovementsLeft < 0 && stationnedPawn != null)) {
            return getNextSquare(pawn);
        } else {
            return getPreviousSquare(pawn);
        }
    }

    public boolean canPawnMove(int diceOutcome) {
        return true;
    }

    public boolean isOccupied() {
        return stationnedPawn != null;
    }
}
