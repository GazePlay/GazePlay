package net.gazeplay.games.horses;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.IGameContext;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressButton;


/**
 * A pawn of the Horses game: its team, its square (none while in its stable), the button choosing it by a fixation and
 * the ring showing that it can be chosen.
 */
public class Pawn {

    @Getter
    private final Horses.TEAMS team;
    private final ImageView pawnDisplay;
    private final ProgressButton button;
    private final Circle ring;
    private final ScaleTransition pulse;
    private final Position initialPosition;
    private final Square startSquare;
    @Getter
    @Setter
    private Square currentSquare;

    /**
     * What choosing the pawn does in this turn; null when it cannot be chosen.
     */
    private Runnable action;

    /**
     * Squares moved since the pawn left its stable: the computer moves the pawn which went the furthest.
     */
    @Getter
    private int progress;

    private int lastThrow;
    private int nbMovementsLeft;
    private int movementOrientation;

    public Pawn(final Horses.TEAMS team, final ImageView pawnDisplay, final ProgressButton button, final Circle ring,
                final Position initialPosition, final Square startSquare, final IGameContext gameContext) {
        this.team = team;
        this.pawnDisplay = pawnDisplay;
        this.button = button;
        this.ring = ring;
        this.initialPosition = initialPosition;
        this.startSquare = startSquare;
        currentSquare = null;
        // The action is given once: giving it at each turn added listeners to the button each time
        button.assignIndicatorUpdatable(e -> choose(), gameContext);
        button.disable();
        ring.setVisible(false);
        pulse = new ScaleTransition(Duration.millis(600), ring);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Moves the pawn to a square, with an animation.
     */
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

    /**
     * Sends the pawn back to its stable.
     */
    public void moveBackToStart() {
        currentSquare = null;
        progress = 0;
        final Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
            new KeyValue(pawnDisplay.layoutXProperty(), initialPosition.getX(), Interpolator.EASE_BOTH),
            new KeyValue(pawnDisplay.layoutYProperty(), initialPosition.getY(), Interpolator.EASE_BOTH)));
        newTimeline.playFromStart();
    }

    /**
     * Puts the pawn on the start square of its team (a pawn of another team there goes back to its stable).
     */
    public void spawn() {
        nbMovementsLeft = 0;
        moveToSquare(startSquare);
    }

    /**
     * @return whether the pawn can leave its stable with this outcome of the die: a 6, and no pawn of its team on the
     *   start square (a pawn of another team there is sent back to its stable)
     */
    public boolean canSpawn(final int diceOutcome) {
        return !isOnTrack() && diceOutcome == 6 && !isOfTeam(startSquare.getStationnedPawn());
    }

    /**
     * @return whether the pawn, on the track, can move with this outcome of the die; a move ending on a pawn of its
     *   team is not allowed
     */
    public boolean canMove(final int diceOutcome) {
        if (!isOnTrack() || !currentSquare.canPawnMove(diceOutcome)) {
            return false;
        }
        final Square destination = destination(diceOutcome);
        return destination != currentSquare && !isOfTeam(destination.getStationnedPawn());
    }

    /**
     * @return whether the move ends on a pawn of another team, sent back to its stable
     */
    public boolean wouldCapture(final int diceOutcome) {
        final Square destination = isOnTrack() ? destination(diceOutcome) : startSquare;
        final Pawn other = destination.getStationnedPawn();
        return other != null && other != this && other.getTeam() != team;
    }

    /**
     * @return whether the pawn is on the final path of its team
     */
    public boolean isOnFinalPath() {
        return currentSquare instanceof FinalPathSquare;
    }

    private boolean isOfTeam(final Pawn other) {
        return other != null && other != this && other.getTeam() == team;
    }

    /**
     * @return the square where the pawn would stop with this outcome of the die, computed as {@link #move()} moves it
     *   (with {@link Square#peekDestination}, which changes nothing)
     */
    Square destination(final int diceOutcome) {
        Square square = currentSquare;
        int left = diceOutcome;
        int orientation = 1;
        while (left > 0) {
            final Square next = square.peekDestination(this, left * orientation, diceOutcome, currentSquare);
            if (next == null) {
                return square;
            }
            orientation = next == square.getPreviousSquare() ? -1 : 1;
            // On the final path, a single step (the movement is cancelled after it)
            left = square instanceof FinalPathSquare && left == diceOutcome ? 0 : left - 1;
            square = next;
        }
        return square;
    }

    /**
     * @return whether the pawn left its stable
     */
    public boolean isOnTrack() {
        return currentSquare != null;
    }

    /**
     * Makes the pawn selectable by a fixation (a pulsing ring of its team around it); choosing it runs the action.
     */
    public void activate(final Runnable action) {
        this.action = action;
        button.setLayoutX(pawnDisplay.getLayoutX());
        button.setLayoutY(pawnDisplay.getLayoutY());
        button.active();
        showRing();
    }

    /**
     * Shows the ring around the pawn, without making it selectable (the move chosen by the computer).
     */
    public void showRing() {
        ring.setCenterX(pawnDisplay.getLayoutX() + pawnDisplay.getFitWidth() / 2);
        ring.setCenterY(pawnDisplay.getLayoutY() + pawnDisplay.getFitHeight() / 2);
        ring.setVisible(true);
        pulse.playFromStart();
    }

    /**
     * Makes the pawn unselectable and hides its ring.
     */
    public void deactivate() {
        action = null;
        button.disable();
        pulse.stop();
        ring.setVisible(false);
    }

    /**
     * The pawn chosen by a fixation: its action runs once.
     */
    void choose() {
        final Runnable chosen = action;
        if (chosen != null) {
            chosen.run();
        }
    }

    /**
     * Moves the pawn by one square, then the next one when the animation ends, until the movement is done.
     */
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

    /**
     * Moves the pawn by a number of squares (out of its stable onto its start square if it is in it).
     */
    public void move(final int nbMovements) {
        if (currentSquare == null) {
            currentSquare = startSquare;
        }
        progress += nbMovements;
        nbMovementsLeft = nbMovements;
        lastThrow = nbMovements;
        movementOrientation = 1;
        move();
    }

    /**
     * Stops the movement of the pawn.
     */
    public void cancelMovement() {
        nbMovementsLeft = 0;
    }
}
