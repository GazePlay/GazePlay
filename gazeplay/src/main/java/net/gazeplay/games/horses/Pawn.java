package net.gazeplay.games.horses;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.ProgressButton;

public class Pawn {

    @Getter
    private Horses.TEAMS team;
    private ProgressButton pawnDisplay;
    private Position initialPosition;
    @Setter
    private Square currentSquare;

    private int lastThrow;
    private int nbMovementsLeft;
    private int movementOrientation;

    public Pawn(Horses.TEAMS team, ProgressButton pawnDisplay, Position initialPosition) {
        this.team = team;
        this.pawnDisplay = pawnDisplay;
        this.initialPosition = initialPosition;
    }

    public void moveToSquare(Square square){
        currentSquare = square;
        Position position = square.getPawnPosition();
        double targetX = position.getX() - pawnDisplay.getWidth() / 2;
        double targetY = position.getY() - pawnDisplay.getHeight() / 2;

        Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(pawnDisplay.layoutXProperty(), targetX, Interpolator.EASE_BOTH),
                new KeyValue(pawnDisplay.layoutYProperty(), targetY, Interpolator.EASE_BOTH)));
        newTimeline.setOnFinished(e -> {
            move();
        });

        newTimeline.playFromStart();
    }

    public void moveBackToStart(){
        currentSquare = null;
    }

    private void move(){
        if(nbMovementsLeft > 0) {
            Square destination = currentSquare.getDestination(this, nbMovementsLeft * movementOrientation, lastThrow);
            if(destination != null && destination != currentSquare){
                moveToSquare(destination);
                nbMovementsLeft--;
            }
        }else{
            currentSquare.pawnLands(this);
        }
    }

    public void move(int nbMovements){
        nbMovementsLeft = nbMovements;
        lastThrow = nbMovements;
        movementOrientation = 1;
        move();
    }

    public void invertMovement(){
        movementOrientation *= -1;
    }

    public void cancelMovement(){
        nbMovementsLeft = 0;
    }
}
