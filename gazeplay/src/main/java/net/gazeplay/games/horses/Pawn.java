package net.gazeplay.games.horses;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.ProgressButton;

public class Pawn {

    @Getter
    private Horses.TEAMS team;
    private ImageView pawnDisplay;
    private ProgressButton button;
    private Position initialPosition;
    private Square startSquare;
    @Setter
    private Square currentSquare;

    private int lastThrow;
    private int nbMovementsLeft;
    private int movementOrientation;

    public Pawn(Horses.TEAMS team, ImageView pawnDisplay, ProgressButton button, Position initialPosition,
            Square startSquare) {
        this.team = team;
        this.pawnDisplay = pawnDisplay;
        this.button = button;
        this.initialPosition = initialPosition;
        this.startSquare = startSquare;
        currentSquare = null;
    }

    public void moveToSquare(Square square) {
        currentSquare = square;
        Position position = square.getPawnPosition();
        double targetX = position.getX() - pawnDisplay.getFitWidth() / 2;
        double targetY = position.getY() - pawnDisplay.getFitHeight() / 2;

        Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(pawnDisplay.layoutXProperty(), targetX, Interpolator.EASE_BOTH),
                new KeyValue(pawnDisplay.layoutYProperty(), targetY, Interpolator.EASE_BOTH)));
        newTimeline.setOnFinished(e -> {
            move();
        });

        newTimeline.playFromStart();
    }

    public void moveBackToStart() {
        currentSquare = null;
        Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(pawnDisplay.layoutXProperty(), initialPosition.getX(), Interpolator.EASE_BOTH),
                new KeyValue(pawnDisplay.layoutYProperty(), initialPosition.getY(), Interpolator.EASE_BOTH)));
        newTimeline.playFromStart();
    }

    public void spawn() {
        moveToSquare(startSquare);
    }

    public boolean canMove(int diceOutcome) {
        return currentSquare.canPawnMove(diceOutcome);
    }

    public boolean isOnTrack() {
        return currentSquare != null;
    }

    public void activate(EventHandler<Event> eventHandler, int fixationLength) {
        button.assignIndicator(eventHandler, fixationLength);
        button.active();
        button.setLayoutX(pawnDisplay.getLayoutX());
        button.setLayoutY(pawnDisplay.getLayoutY());
    }

    public void deactivate() {
        button.setLayoutX(-1000);
        button.setLayoutY(-1000);
    }

    private void move() {
        if (nbMovementsLeft > 0) {
            Square destination = currentSquare.getDestination(this, nbMovementsLeft * movementOrientation, lastThrow);
            if (destination == currentSquare.getPreviousSquare()) {
                movementOrientation = -1;
            } else {
                movementOrientation = 1;
            }

            if (destination != null) {
                moveToSquare(destination);
                nbMovementsLeft--;
            } else {
                currentSquare.pawnLands(this);
            }
        } else {
            currentSquare.pawnLands(this);
        }
    }

    public void move(int nbMovements) {
        if (currentSquare == null) {
            currentSquare = startSquare;
        }
        nbMovementsLeft = nbMovements;
        lastThrow = nbMovements;
        movementOrientation = 1;
        move();
    }

    public void cancelMovement() {
        nbMovementsLeft = 0;
    }
}
