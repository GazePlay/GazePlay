package net.gazeplay.games.horses;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.components.Position;


public class Square {

    @Getter
    @Setter
    private Square nextSquare;
    @Getter
    @Setter
    private Square previousSquare;
    @Getter
    private final Position pawnPosition;
    protected Pawn stationnedPawn;
    protected final Horses game;

    public Square(final Position pawnPosition, final Horses game) {
        this.pawnPosition = pawnPosition;
        this.game = game;
        stationnedPawn = null;
    }

    protected Square getNextSquare(final Pawn pawn) {
        return nextSquare;
    }

    protected Square getPreviousSquare(final Pawn pawn) {
        return previousSquare;
    }

    public void pawnLands(final Pawn pawn) {
        if (stationnedPawn != null && stationnedPawn != pawn && stationnedPawn.getTeam() != pawn.getTeam()) {
            stationnedPawn.moveBackToStart();
        } else if (stationnedPawn != null) {
            pawn.moveToSquare(getPreviousSquare());
        }
        stationnedPawn = pawn;
        game.endOfTurn();
    }

    public Square getDestination(final Pawn pawn, final int nbMovementsLeft, final int nbMovementsTotal) {
        if (nbMovementsLeft == nbMovementsTotal) {
            stationnedPawn = null;
        }
        if ((nbMovementsLeft > 0 && stationnedPawn == null) || (nbMovementsLeft < 0 && stationnedPawn != null)) {
            return getNextSquare(pawn);
        } else {
            return getPreviousSquare(pawn);
        }
    }

    public boolean canPawnMove(final int diceOutcome) {
        return true;
    }

    public boolean isOccupied() {
        return stationnedPawn != null;
    }
}
