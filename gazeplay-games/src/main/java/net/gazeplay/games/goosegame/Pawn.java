package net.gazeplay.games.goosegame;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.components.Position;


public class Pawn {

    @Getter
    private ImageView pawnDisplay;
    @Getter
    private int turnsLeftToSkip;
    @Getter
    private int lastThrowResult;
    @Getter
    private Square currentSquare;
    private int nbMovementsLeft;
    @Getter
    private int number;
    private boolean movementStart;

    public Pawn(ImageView pawnDisplay, Square startSquare, int number) {
        this.pawnDisplay = pawnDisplay;
        this.turnsLeftToSkip = 0;
        this.number = number;
        reset(startSquare);
        nbMovementsLeft = 0;
    }

    public void reset(Square startSquare) {
        this.currentSquare = startSquare;
        pawnDisplay.setX(startSquare.getPawnPosition().getX() - pawnDisplay.getFitWidth() / 2);
        pawnDisplay.setY(startSquare.getPawnPosition().getY() - pawnDisplay.getFitHeight() / 2);
    }

    public void move(int nbMovementsLeft) {
        this.lastThrowResult = nbMovementsLeft;
        this.nbMovementsLeft = nbMovementsLeft;
        movementStart = true;
        move();
    }

    private void move() {
        if (nbMovementsLeft == 0) {
            currentSquare.pawnStays(this);
        } else if (nbMovementsLeft > 0) {
            if (currentSquare.getNextSquare() == null) {
                moveToSquare(currentSquare.getPreviousSquare());
                nbMovementsLeft = -nbMovementsLeft + 1;
            } else {
                moveToSquare(currentSquare.getNextSquare());
                nbMovementsLeft--;
            }
        } else {
            if (currentSquare.getPreviousSquare() == null) {
                nbMovementsLeft = 0;
                currentSquare.pawnStays(this);
            } else {
                moveToSquare(currentSquare.getPreviousSquare());
                nbMovementsLeft++;
            }
        }
    }

    public void skipTurns(int nbTurns) {
        turnsLeftToSkip = nbTurns;
    }

    public void imprison() {
        turnsLeftToSkip = -1;
    }

    public void free() {
        turnsLeftToSkip = 0;
    }

    public boolean isStuck() {
        return turnsLeftToSkip == -1;
    }

    public boolean isSleeping() {
        if (turnsLeftToSkip > 0) {
            turnsLeftToSkip--;
            return true;
        } else {
            return false;
        }
    }

    public void moveToSquare(Square square) {
        currentSquare = square;
        Position position = square.getPawnPosition();
        double targetX = position.getX() - pawnDisplay.getFitWidth() / 2 + Math.cos(number * 2 * Math.PI / 5) * 10;
        double targetY = position.getY() - pawnDisplay.getFitHeight() / 2 + Math.sin(number * 2 * Math.PI / 5) * 10;

        Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(pawnDisplay.xProperty(), targetX, Interpolator.EASE_BOTH),
                new KeyValue(pawnDisplay.yProperty(), targetY, Interpolator.EASE_BOTH)));
        newTimeline.setOnFinished(e -> {
            square.pawnPassesBy(this);
            move();
        });

        if (movementStart) {
            movementStart = false;
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(0.5)));
            delay.setOnFinished(e -> newTimeline.playFromStart());
            delay.playFromStart();
        } else {
            newTimeline.playFromStart();
        }
    }
}
