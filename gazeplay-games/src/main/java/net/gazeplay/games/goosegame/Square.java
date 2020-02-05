package net.gazeplay.games.goosegame;

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

    protected void pawnStays(final Pawn pawn) {
        game.endOfTurn();
    }

    protected void pawnPassesBy(final Pawn pawn) {
        game.playMovementSound();
    }

}
