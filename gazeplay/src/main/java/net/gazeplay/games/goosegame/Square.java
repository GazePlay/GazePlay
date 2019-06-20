package net.gazeplay.games.goosegame;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.utils.Position;

public class Square {

    @Setter
    private Square nextSquare;
    private Square previousSquare;
    private int number;
    @Getter
    private Position pawnPosition;
    private GooseGame game;

    public Square(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        this.previousSquare = previousSquare;
        this.number = number;
        this.pawnPosition = pawnPosition;
        this.game = game;
    }

    public void moveForward(Pawn pawn, int nbMovementsLeft){
        pawn.moveToSquare(this);
        if(nbMovementsLeft == 0){
            act(pawn);
        }else if(nextSquare == null){
            previousSquare.moveBackward(pawn, nbMovementsLeft - 1);
        }else{
            nextSquare.moveForward(pawn, nbMovementsLeft - 1);
        }
    }

    public void moveBackward(Pawn pawn, int nbMovementsLeft){
        pawn.moveToSquare(this);
        if(nbMovementsLeft == 0 || previousSquare == null){
            act(pawn);
        }else{
            previousSquare.moveBackward(pawn, nbMovementsLeft - 1);
        }
    }

    private void act(Pawn pawn){
        pawn.endOfTurn();
    }

}
