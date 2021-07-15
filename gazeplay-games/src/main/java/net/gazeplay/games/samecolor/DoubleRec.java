package net.gazeplay.games.samecolor;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.List;

public class DoubleRec {

    public Rectangle rec1;
    public Rectangle rec2;

    public Color color;

    private boolean isin1m;
    private boolean isin2m;
    private boolean isin1g;
    private boolean isin2g;

    private boolean tofits;


    DoubleRec(double x1, double y1, double x2, double y2, double width, double height, Color color){
        this.color = color;
        rec1 = new Rectangle(x1, y1, width, height);
        rec2 = new Rectangle(x2, y2, width, height);
        rec1.setFill(color);
        rec2.setFill(color);
        isin1m = false;
        isin2m = false;
        isin1g = false;
        isin2g = false;
        tofits = false;
    }

    public void setEvent(javafx.event.EventHandler<ActionEvent> eventwin, GazeDeviceManager gazeDeviceManager, IGameContext gameContext, List<DoubleRec> list, ReplayablePseudoRandom random){
        rec1.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            isin1m = true;
            sameselect(eventwin, gameContext, list, random);
        });
        rec2.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            isin2m = true;
            sameselect(eventwin, gameContext, list, random);
        });
        rec1.addEventFilter(MouseEvent.MOUSE_EXITED, e -> isin1m = false);
        rec2.addEventFilter(MouseEvent.MOUSE_EXITED, e -> isin2m = false);

        rec1.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
            isin1g = true;
            sameselect(eventwin, gameContext, list, random);
        });
        rec2.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
            isin2g = true;
            sameselect(eventwin, gameContext, list, random);
        });
        rec1.addEventHandler(GazeEvent.GAZE_EXITED, e -> isin1g = false);
        rec2.addEventHandler(GazeEvent.GAZE_EXITED, e -> isin2g = false);
        gazeDeviceManager.addEventFilter(rec1);
        gazeDeviceManager.addEventFilter(rec2);
    }

    private void sameselect(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list, ReplayablePseudoRandom random){
        gameContext.getChildren().removeAll(rec1, rec2);
        gameContext.getChildren().addAll(rec1, rec2);
        if ((isin1m || isin1g) && (isin2m || isin2g) && !tofits){
            tofits = true;
            fits(eventwin, gameContext, list, random, 10);
            /*gameContext.getChildren().removeAll(rec1, rec2);
            list.remove(this);
            PauseTransition Win = new PauseTransition(Duration.millis(10));
            Win.setOnFinished(eventwin);
            Win.play();*/
        }
    }

    private void fits(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list, ReplayablePseudoRandom random, int n){
        /*if (!(isin1m || isin1g) || !(isin2m || isin2g) || shake>=10){
            fitsoff(eventwin, gameContext, list);
        }*/
        if (n>0) {
            PauseTransition move1 = new PauseTransition(Duration.millis(250));
            PauseTransition move2 = new PauseTransition(Duration.millis(250));
            int x = (random.nextInt(100) - 50)/3;
            int y = (random.nextInt(100) - 50)/3;
            move1.setOnFinished(e -> {
                rec1.setX(rec1.getX() + x);
                rec1.setY(rec1.getY() + y);
                rec2.setX(rec2.getX() + x);
                rec2.setY(rec2.getY() + y);
                move2.play();
            });
            move2.setOnFinished(e -> {
                rec1.setX(rec1.getX() - x);
                rec1.setY(rec1.getY() - y);
                rec2.setX(rec2.getX() - x);
                rec2.setY(rec2.getY() - y);
                fits(eventwin, gameContext, list, random, n-1);
            });
            move1.play();
        } else {
            fitsoff(eventwin, gameContext, list);
        }
    }

    private void fitsoff(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list){
        if (list.contains(this)){
            gameContext.getChildren().removeAll(rec1, rec2);
            list.remove(this);
            PauseTransition Win = new PauseTransition(Duration.millis(10));
            Win.setOnFinished(eventwin);
            Win.play();
        }
    }
}
