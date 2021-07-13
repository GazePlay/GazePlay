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

    public boolean isin1;
    public boolean isin2;

    private int shake;

    DoubleRec(double x1, double y1, double x2, double y2, double width, double height, Color color){
        this.color = color;
        rec1 = new Rectangle(x1, y1, width, height);
        rec2 = new Rectangle(x2, y2, width, height);
        rec1.setFill(color);
        rec2.setFill(color);
        isin1 = false;
        isin2 = false;
        shake = 0;
    }

    public void setEvent(javafx.event.EventHandler<ActionEvent> eventwin, GazeDeviceManager gazeDeviceManager, IGameContext gameContext, List<DoubleRec> list, ReplayablePseudoRandom random){
        rec1.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            isin1 = true;
            sameselect(eventwin, gameContext, list, random);
        });
        rec2.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            isin2 = true;
            sameselect(eventwin, gameContext, list, random);
        });
        //rec1.addEventFilter(MouseEvent.MOUSE_EXITED, e -> isin1 = false);
        //rec2.addEventFilter(MouseEvent.MOUSE_EXITED, e -> isin2 = false);

        rec1.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
            isin1 = true;
            sameselect(eventwin, gameContext, list, random);
        });
        rec2.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
            isin2 = true;
            sameselect(eventwin, gameContext, list, random);
        });
        //rec1.addEventHandler(GazeEvent.GAZE_EXITED, e -> isin1 = false);
        //rec2.addEventHandler(GazeEvent.GAZE_EXITED, e -> isin2 = false);
        gazeDeviceManager.addEventFilter(rec1);
        gazeDeviceManager.addEventFilter(rec2);
    }

    private void sameselect(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list, ReplayablePseudoRandom random){
        gameContext.getChildren().removeAll(rec1, rec2);
        gameContext.getChildren().addAll(rec1, rec2);
        if (isin1 && isin2){
            fits(eventwin, gameContext, list, random);
        }
        if (shake>=10){
            gameContext.getChildren().removeAll(rec1, rec2);
            list.remove(this);
            PauseTransition Win = new PauseTransition(Duration.millis(10));
            Win.setOnFinished(eventwin);
            Win.play();
        } else {
            shake = 0;
        }
    }

    private void fits(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list, ReplayablePseudoRandom random){
        if (!isin1 || !isin2 || shake>=10){
            fitsoff(eventwin, gameContext, list);
        }
        else {
            PauseTransition move1 = new PauseTransition(Duration.millis(250));
            PauseTransition move2 = new PauseTransition(Duration.millis(250));
            int x = (random.nextInt(100) - 50);
            int y = (random.nextInt(100) - 50);
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
                shake++;
                fits(eventwin, gameContext, list, random);
            });
            move1.play();
        }
    }

    private void fitsoff(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list){
        if (shake>=10){
            gameContext.getChildren().removeAll(rec1, rec2);
            list.remove(this);
            PauseTransition Win = new PauseTransition(Duration.millis(10));
            Win.setOnFinished(eventwin);
            Win.play();
        } else {
            shake = 0;
        }
    }
}
