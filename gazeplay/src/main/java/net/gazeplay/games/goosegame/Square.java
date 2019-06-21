package net.gazeplay.games.goosegame;

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
    protected int number;
    @Getter
    private Position pawnPosition;
    protected GooseGame game;

    public Square(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        this.previousSquare = previousSquare;
        this.number = number;
        this.pawnPosition = pawnPosition;
        this.game = game;
    }

    protected void pawnStays(Pawn pawn){
        game.endOfTurn();
    }

    protected void pawnPassesBy(Pawn pawn){

    }

}
