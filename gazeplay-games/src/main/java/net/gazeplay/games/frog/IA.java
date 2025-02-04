package net.gazeplay.games.frog;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@Slf4j
public class IA {

    Frog frog;
    IGameContext gameContext;
    ArrayList<Integer> possiblePosition;
    Boolean firstJump = true;
    int pastFrogPosition;
    int futureFrogPosition;
    String moveType = "";
    Timeline autoMove;
    public IA(Frog frog, IGameContext gameContext){
        this.frog = frog;
        this.gameContext = gameContext;
        this.possiblePosition = new ArrayList<>(Arrays.asList(2,3,7,8));

        this.createTimeline();
    }

    public void iaMoves(int iteration){
        if (iteration == 1){
            this.moveOneBack();
        }else if (iteration == 7){
            this.moveOneFront();
        }else if (iteration == 12){
            this.moveTwoBack();
        }else if (iteration == 19){
            this.moveOneBackAgain();
        }else if (iteration == 27){
            this.moveJump();
        }else if (iteration == 34){
            this.frog.dispose();
        }else {
            this.frog.setGoodAnswer(this.moveType);
            this.frog.playerTurn();
        }
    }

    public void createTimeline(){
        autoMove = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            this.updateFrogPosition();
            this.frog.moveFrogTo(this.frog.nenuphars[this.frog.frogPosition]);
        }));

        autoMove.setOnFinished(event -> {
            autoMove.stop();
            this.firstJump = true;
            this.frog.setGoodAnswer(this.moveType);
            this.frog.playerTurn();
        });

        autoMove.setCycleCount(3);
    }

    public void moveOneBack(){
        this.moveType = "oneBack";
        autoMove.playFromStart();
    }

    public void moveOneBackAgain(){
        this.moveType = "oneBack";
        this.frog.setGoodAnswer(this.moveType);
        this.frog.playerTurn();
    }

    public void moveOneFront(){
        this.moveType = "oneFront";
        this.frog.setGoodAnswer(this.moveType);
        this.frog.playerTurn();
    }

    public void moveTwoBack(){
        this.moveType = "twoBack";
        this.frog.setGoodAnswer(this.moveType);
        this.frog.playerTurn();
    }

    public void moveJump(){
        this.moveType = "jump";
        this.frog.setGoodAnswer(this.moveType);
        this.frog.playerTurn();
    }

    public void updateFrogPosition(){
        this.frog.frogPosition -= 1;
        if (this.frog.frogPosition < 0){
            this.frog.frogPosition = this.frog.nenuphars.length - 1;
        }
    }
}
