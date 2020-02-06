package net.gazeplay.games.horses;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressButton;


public class Pawn {

    @Getter
    private final Horses.TEAMS team;
    private final ImageView pawnDisplay;
    private final ProgressButton button;
    private final Position initialPosition;
    private final Square startSquare;
    @Setter
    private Square currentSquare;

    private int lastThrow;
    private int nbMovementsLeft;
    private int movementOrientation;

    public Pawn(final Horses.TEAMS team, final ImageView pawnDisplay, final ProgressButton button, final Position initialPosition,
                final Square startSquare) {
        this.team = team;
        this.pawnDisplay = pawnDisplay;
        this.button = button;
        this.initialPosition = initialPosition;
        this.startSquare = startSquare;
        currentSquare = null;
    }

    public void moveToSquare(final Square square) {
        currentSquare = square;
        final Position position = square.getPawnPosition();
        final double targetX = position.getX() - pawnDisplay.getFitWidth() / 2;
        final double targetY = position.getY() - pawnDisplay.getFitHeight() / 2;

        final Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
            new KeyValue(pawnDisplay.layoutXProperty(), targetX, Interpolator.EASE_BOTH),
            new KeyValue(pawnDisplay.layoutYProperty(), targetY, Interpolator.EASE_BOTH)));
        newTimeline.setOnFinished(e -> move());

        newTimeline.playFromStart();
    }

    public void moveBackToStart() {
        currentSquare = null;
        final Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
            new KeyValue(pawnDisplay.layoutXProperty(), initialPosition.getX(), Interpolator.EASE_BOTH),
            new KeyValue(pawnDisplay.layoutYProperty(), initialPosition.getY(), Interpolator.EASE_BOTH)));
        newTimeline.playFromStart();
    }

    public void spawn() {
        moveToSquare(startSquare);
    }

    public boolean canMove(final int diceOutcome) {
        return currentSquare.canPawnMove(diceOutcome);
    }

    public boolean isOnTrack() {
        return currentSquare != null;
    }

    public void activate(final EventHandler<Event> eventHandler, final int fixationLength) {
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
            final Square destination = currentSquare.getDestination(this, nbMovementsLeft * movementOrientation, lastThrow);
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

    public void move(final int nbMovements) {
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
