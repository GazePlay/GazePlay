package net.gazeplay.games.horses;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.components.Position;


/**
 * A square of the track of the Horses game, linked to the previous and next squares.
 */
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

    /**
     * A pawn stops on the square: a pawn of another team already there goes back to its stable. A move ending on a
     * pawn of the same team is not offered (see {@link Pawn#canMove}); it used to make the pawn step back, which ended
     * the turn twice (a team missed its turn).
     */
    public void pawnLands(final Pawn pawn) {
        if (stationnedPawn != null && stationnedPawn != pawn && stationnedPawn.getTeam() != pawn.getTeam()) {
            stationnedPawn.moveBackToStart();
        }
        stationnedPawn = pawn;
        game.endOfTurn();
    }

    /**
     * @return the pawn on the square, null if none
     */
    public Pawn getStationnedPawn() {
        return stationnedPawn;
    }

    /**
     * The same as {@link #getDestination}, without changing anything: used to know where a move would end.
     *
     * @param departure the square the pawn leaves (counted as empty, as {@link #getDestination} empties it)
     */
    public Square peekDestination(final Pawn pawn, final int nbMovementsLeft, final int nbMovementsTotal,
                                  final Square departure) {
        final boolean occupied = this != departure && stationnedPawn != null;
        if ((nbMovementsLeft > 0 && !occupied) || (nbMovementsLeft < 0 && occupied)) {
            return getNextSquare(pawn);
        } else {
            return getPreviousSquare(pawn);
        }
    }

    /**
     * @return the next square of a pawn moving over this one, or null when it cannot move
     */
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

    /**
     * @return whether a pawn on this square can move with this outcome of the die
     */
    public boolean canPawnMove(final int diceOutcome) {
        return true;
    }

    /**
     * @return whether a pawn is on the square
     */
    public boolean isOccupied() {
        return stationnedPawn != null;
    }
}
