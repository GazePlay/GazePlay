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

import java.util.List;

public class DoubleRec {

    public Rectangle rec1;
    public Rectangle rec2;

    public Color color;

    public boolean isin1;
    public boolean isin2;

    DoubleRec(double x1, double y1, double x2, double y2, double width, double height, Color color){
        this.color = color;
        rec1 = new Rectangle(x1, y1, width, height);
        rec2 = new Rectangle(x2, y2, width, height);
        rec1.setFill(color);
        rec2.setFill(color);
        isin1 = false;
        isin2 = false;
    }

    public void setEvent(javafx.event.EventHandler<ActionEvent> eventwin, GazeDeviceManager gazeDeviceManager, IGameContext gameContext, List<DoubleRec> list){
        rec1.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            isin1 = true;
            sameselect(eventwin, gameContext, list);
        });
        rec2.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            isin2 = true;
            sameselect(eventwin, gameContext, list);
        });
        //rec1.addEventFilter(MouseEvent.MOUSE_EXITED, e -> isin1 = false);
        //rec2.addEventFilter(MouseEvent.MOUSE_EXITED, e -> isin2 = false);

        rec1.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
            isin1 = true;
            sameselect(eventwin, gameContext, list);
        });
        rec2.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
            isin2 = true;
            sameselect(eventwin, gameContext, list);
        });
        //rec1.addEventHandler(GazeEvent.GAZE_EXITED, e -> isin1 = false);
        //rec2.addEventHandler(GazeEvent.GAZE_EXITED, e -> isin2 = false);
        gazeDeviceManager.addEventFilter(rec1);
        gazeDeviceManager.addEventFilter(rec2);
    }

    private void sameselect(javafx.event.EventHandler<ActionEvent> eventwin, IGameContext gameContext, List<DoubleRec> list){
        if (isin1 && isin2){
            gameContext.getChildren().removeAll(rec1, rec2);
            list.remove(this);
            PauseTransition Win = new PauseTransition(Duration.millis(10));
            Win.setOnFinished(eventwin);
            Win.play();
        }
    }
}
