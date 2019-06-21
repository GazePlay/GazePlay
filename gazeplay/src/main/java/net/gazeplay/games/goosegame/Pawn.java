package net.gazeplay.games.goosegame;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.utils.Position;

public class Pawn {

    private ImageView pawnDisplay;
    private int turnsLeftToSkip;
    @Getter
    private int lastThrowResult;
    @Getter
    private Square currentSquare;
    private int nbMovementsLeft;

    public Pawn(ImageView pawnDisplay, Square startSquare) {
        this.pawnDisplay = pawnDisplay;
        this.turnsLeftToSkip = 0;
        reset(startSquare);
        nbMovementsLeft = 0;
    }

    public void reset(Square startSquare){
        this.currentSquare = startSquare;
        pawnDisplay.setX(startSquare.getPawnPosition().getX() - pawnDisplay.getFitWidth()/2);
        pawnDisplay.setY(startSquare.getPawnPosition().getY() - pawnDisplay.getFitHeight()/2);
    }

    public void move(int nbMovementsLeft){
        this.lastThrowResult = nbMovementsLeft;
        this.nbMovementsLeft = nbMovementsLeft;
        move();
    }

    private void move(){
        if(nbMovementsLeft == 0){
            currentSquare.pawnStays(this);
        }else if (nbMovementsLeft > 0) {
            if(currentSquare.getNextSquare() == null) {
                moveToSquare(currentSquare.getPreviousSquare());
                nbMovementsLeft = -nbMovementsLeft + 1;
            }else{
                moveToSquare(currentSquare.getNextSquare());
                nbMovementsLeft--;
            }
        }else{
            if(currentSquare.getPreviousSquare() == null) {
                nbMovementsLeft = 0;
                currentSquare.pawnStays(this);
            }else {
                moveToSquare(currentSquare.getPreviousSquare());
                nbMovementsLeft++;
            }
        }
    }

    public void skipTurns(int nbTurns){
        turnsLeftToSkip = nbTurns;
    }

    public void imprison(){
        turnsLeftToSkip = -1;
    }

    public void free(){
        turnsLeftToSkip = 0;
    }

    public boolean canPlay(){
        if(turnsLeftToSkip > 0){
            turnsLeftToSkip--;
        }else if(turnsLeftToSkip == 0){
            return true;
        }
        return false;
    }

    public void moveToSquare(Square square){
        currentSquare = square;
        square.pawnPassesBy(this);
        Position position = square.getPawnPosition();
        double targetX = position.getX() - pawnDisplay.getFitWidth()/2;
        double targetY = position.getY() - pawnDisplay.getFitHeight()/2;

        Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), new KeyValue(pawnDisplay.xProperty(), targetX, Interpolator.EASE_BOTH), new KeyValue(pawnDisplay.yProperty(), targetY, Interpolator.EASE_BOTH)));
        newTimeline.setOnFinished(e -> move());

        newTimeline.playFromStart();
    }
}
