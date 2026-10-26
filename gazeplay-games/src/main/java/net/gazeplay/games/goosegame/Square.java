package net.gazeplay.games.goosegame;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.components.Position;


/**
 * A square of the Goose game board, linked to the previous and next squares. Subclasses change what happens to a pawn
 * stopping on them or passing by.
 */
public class Square {

    @Getter
    @Setter
    private Square nextSquare;
    @Getter
    @Setter
    private Square previousSquare;
    protected final int number;
    @Getter
    private final Position pawnPosition;
    protected final GooseGame game;

    public Square(final int number, final Position pawnPosition, final Square previousSquare, final GooseGame game) {
        this.previousSquare = previousSquare;
        this.number = number;
        this.pawnPosition = pawnPosition;
        this.game = game;
    }

    /**
     * What happens when a pawn stops on the square: by default, the turn ends.
     */
    protected void pawnStays(final Pawn pawn) {
        game.endOfTurn();
    }

    /**
     * What happens when a pawn passes over the square: by default, a sound.
     */
    protected void pawnPassesBy(final Pawn pawn) {
        game.playMovementSound();
    }

}
