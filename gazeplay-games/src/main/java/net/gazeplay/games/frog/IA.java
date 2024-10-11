package net.gazeplay.games.frog;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

@Slf4j
public class IA {

    Frog frog;
    IGameContext gameContext;

    String moveType = "";
    Timeline autoMove;

    public IA(Frog frog, IGameContext gameContext){
        this.frog = frog;
        this.gameContext = gameContext;

        this.createTimeline();
    }

    public void iaMoves(int iteration){
        if (iteration < 7){
            log.info("Move one back");
            this.moveOneBack();
        }else if (iteration < 12){
            log.info("Move one front");
            this.moveOneFront();
        }else if (iteration < 19){
            log.info("Move two back");
            this.moveTwoBack();
        }else if (iteration < 27){
            log.info("Move one back again");
            this.moveOneBack();
        }/*else if (iteration < 33){
            log.info("Move jump");
            this.moveJump();
        }*/else {
            this.frog.dispose();
        }
    }

    public void createTimeline(){
        autoMove = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            log.info("Timeline Cycle");
            this.updateFrogPosition();
            this.frog.moveFrogTo(this.frog.nenuphars[this.frog.frogPosition]);
        }));

        autoMove.setOnFinished(event -> {
            autoMove.stop();
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

    public void updateFrogPosition(){
        switch (this.moveType){
            case "oneBack":
                this.frog.frogPosition -= 1;
                if (this.frog.frogPosition < 0){
                    this.frog.frogPosition = this.frog.nenuphars.length - 1;
                }
                log.info("New frog position -> " + this.frog.frogPosition);
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

                break;

            default:
                break;
        }
    }

    public void checkNextJump(){

    }
}
