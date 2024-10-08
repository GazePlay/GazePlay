package net.gazeplay.games.frog;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

import java.util.Random;

@Slf4j
public class IA {

    Frog frog;
    IGameContext gameContext;

    int nbDuration = 2;
    Timeline autoMove;

    public IA(Frog frog, IGameContext gameContext){
        this.frog = frog;
        this.gameContext = gameContext;

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
            this.moveJump();
        }else {
            this.frog.dispose();
        }
    }

    public void createTimeline(){
        autoMove = new Timeline(new KeyFrame(Duration.seconds(nbDuration), event -> {
            this.updateMoveOneBackFrogPosition();
            this.frog.moveFrogTo(this.frog.nenuphars[this.frog.frogPosition]);
        }));

        autoMove.setOnFinished(event -> {
            this.frog.playerTurn();
            autoMove.stop();
        });
    }

    public void moveOneBack(){
        Random random = new Random();
        int nbMove = 2 + random.nextInt(3);

        autoMove.setCycleCount(nbMove);
        autoMove.playFromStart();
    }

    public void moveOneFront(){

    }

    public void moveTwoBack(){

    }

    public void moveJump(){

    }

    public void updateMoveOneBackFrogPosition(){
        this.frog.frogPosition -= 1;
        if (this.frog.frogPosition < 0){
            this.frog.frogPosition = this.frog.nenuphars.length - 1;
        }
    }
}
