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
    private Timeline lastTimeline;
    private int turnsLeftToSkip;
    private boolean canPlay;
    @Getter
    @Setter
    private int lastThrowResult;
    @Getter
    private Square currentSquare;

    public Pawn(ImageView pawnDisplay, Square startSquare) {
        this.pawnDisplay = pawnDisplay;
        this.turnsLeftToSkip = 0;
        this.canPlay = true;
        this.currentSquare = startSquare;
        pawnDisplay.setX(startSquare.getPawnPosition().getX() - pawnDisplay.getFitWidth()/2);
        pawnDisplay.setY(startSquare.getPawnPosition().getY() - pawnDisplay.getFitHeight()/2);
    }

    public void skipTurns(int nbTurns){
        turnsLeftToSkip = nbTurns;
        canPlay = false;
    }

    public void moveToSquare(Square square){
        currentSquare = square;
        Position position = square.getPawnPosition();
        double targetX = position.getX() - pawnDisplay.getFitWidth()/2;
        double targetY = position.getY() - pawnDisplay.getFitHeight()/2;

        Timeline newTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), new KeyValue(pawnDisplay.xProperty(), targetX, Interpolator.EASE_BOTH), new KeyValue(pawnDisplay.yProperty(), targetY, Interpolator.EASE_BOTH)));
        if(lastTimeline != null){
            lastTimeline.setOnFinished(e -> newTimeline.playFromStart());
        }else{
            newTimeline.playFromStart();
        }
        lastTimeline = newTimeline;
    }

    public void endOfTurn(){
        lastTimeline = null;
    }
}
