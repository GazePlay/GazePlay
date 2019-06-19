package net.gazeplay.games.goosegame;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Setter;

public class Square extends StackPane {

    @Setter
    private Square nextSquare;
    private Square previousSquare;

    public Square(int number, double width, Square previousSquare) {
        this.previousSquare = previousSquare;

        Rectangle background = new Rectangle(width, width, Color.WHITE);
        background.setStroke(Color.BLUE);
        Text numberText = new Text(number+"");
        this.getChildren().addAll(background, numberText);

    }

    public void moveForward(int nbMovementsLeft){
        if(nbMovementsLeft != 0){
            act();
        }else if(nextSquare == null){
            previousSquare.moveBackward(nbMovementsLeft - 1);
        }else{
            nextSquare.moveForward(nbMovementsLeft - 1);
        }
    }

    public void moveBackward(int nbMovementsLeft){
        if(nbMovementsLeft != 0 || previousSquare == null){
            act();
        }else{
            previousSquare.moveBackward(nbMovementsLeft - 1);
        }
    }

    private void act(){

    }

}
