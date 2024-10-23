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
    int cyclePassed = 0;

    public IA(Frog frog, IGameContext gameContext){
        this.frog = frog;
        this.gameContext = gameContext;
        this.possiblePosition = new ArrayList<>(Arrays.asList(2,3,7,8));

        this.createTimeline();
    }

    public void iaMoves(int iteration){
        if (iteration < 7){
            this.moveOneBack();
        }else if (iteration < 12){
            this.moveOneFront();
        }else if (iteration < 19){
            this.moveTwoBack();
        }else if (iteration < 27){
            this.moveOneBack();
        }else if (iteration < 33){
            this.specialMoveFrog();
            this.moveJump();
        }else {
            this.frog.dispose();
        }
    }

    public void createTimeline(){
        autoMove = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (this.cyclePassed < 2){
                this.cyclePassed++;
                this.updateFrogPosition();
                this.frog.moveFrogTo(this.frog.nenuphars[this.frog.frogPosition]);
            }
        }));

        autoMove.setOnFinished(event -> {
            autoMove.stop();
            this.firstJump = true;
            this.cyclePassed = 0;
            this.frog.setGoodAnswer(this.moveType);
            this.frog.playerTurn();
        });

        autoMove.setCycleCount(3);
    }

    public void moveOneBack(){
        this.moveType = "oneBack";
        autoMove.playFromStart();
    }

    public void moveOneFront(){
        this.moveType = "oneFront";
        autoMove.playFromStart();
    }

    public void moveTwoBack(){
        this.moveType = "twoBack";
        autoMove.playFromStart();
    }

    public void moveJump(){
        this.moveType = "jump";
        autoMove.playFromStart();
    }

    public void specialMoveFrog(){
        if (!this.possiblePosition.contains(this.frog.frogPosition)){
            Random randomPos = new Random();
            int newPos = this.possiblePosition.get(randomPos.nextInt(this.possiblePosition.size()));
            this.frog.moveFrogTo(this.frog.nenuphars[newPos]);
            this.frog.frogPosition = newPos;
        }
    }

    public void updateFrogPosition(){
        switch (this.moveType){
            case "oneBack":
                this.frog.frogPosition -= 1;
                if (this.frog.frogPosition < 0){
                    this.frog.frogPosition = this.frog.nenuphars.length - 1;
                }
                break;

            case "oneFront":
                this.frog.frogPosition += 1;
                if (this.frog.frogPosition > 9){
                    this.frog.frogPosition = 0;
                }
                break;

            case "twoBack":
                this.frog.frogPosition -= 2;
                if (this.frog.frogPosition == -1){
                    this.frog.frogPosition = this.frog.nenuphars.length - 1;
                }else {
                    this.frog.frogPosition = this.frog.nenuphars.length - 2;
                }
                break;

            case "jump":
                if (this.firstJump){
                    this.firstJump = false;
                    this.pastFrogPosition = this.frog.frogPosition;

                    Random randomJump = new Random();
                    int newPosJump;

                    if ((this.frog.frogPosition == 2) || (this.frog.frogPosition == 3)){
                        newPosJump = randomJump.nextInt(2) + 7;

                    }else {
                        newPosJump = randomJump.nextInt(2) + 2;
                    }

                    this.futureFrogPosition = newPosJump;
                    this.frog.frogPosition = newPosJump;
                }else {
                    this.frog.frogPosition = this.pastFrogPosition;
                }
                break;

            default:
                break;
        }
    }
}
