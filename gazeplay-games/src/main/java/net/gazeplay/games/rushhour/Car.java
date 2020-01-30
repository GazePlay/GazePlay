package net.gazeplay.games.rushhour;

import com.sun.glass.ui.Screen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import tobii.Tobii;

import java.awt.*;

@Slf4j
public class Car extends Rectangle {

    @Setter
    private boolean endOfGame = false;

    @Getter
    private Orientation orientation;
    private int size;
    private int x;
    private int y;
    private int l;
    private int h;

    @Setter
    private boolean intersect = false;
    private Timeline timelineProgressBar;
    IGameContext gameContext;
    private ProgressIndicator pi;

    private boolean selected = false;

    static enum Orientation{
        horizontal,
        vertical
    }

    Car(int carX, int carY, int carLength, int carHeight, Color carColor, Orientation orientation, int blockSize, ProgressIndicator pi,
        IGameContext gameContext) {
        super(0, 0, carLength * blockSize, carHeight * blockSize);
        if (orientation == Orientation.horizontal) {
            this.setFill(new ImagePattern(new Image("data/rushHour/carH.png")));
        } else if (orientation == Orientation.vertical) {
            this.setFill(new ImagePattern(new Image("data/rushHour/carV.png")));
        }

        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(100.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(30, 30, carColor));

        this.setEffect(lighting);
        this.gameContext = gameContext;
        this.orientation = orientation;
        this.size = blockSize;
        this.setOpacity(0.7);
        this.pi = pi;
        this.l = carLength;
        this.h = carHeight;
        this.x = carX;
        this.y = carY;

        EventHandler<Event> enterEvent = e -> {
            pi.setLayoutX(getX() + getWidth() / 2 - pi.getWidth() / 2);
            pi.setLayoutY(getY() + getHeight() / 2 - pi.getHeight() / 2);
            pi.setOpacity(1);
            pi.toFront();
            timelineProgressBar = new Timeline();
            timelineProgressBar.getKeyFrames()
                .add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(pi.progressProperty(), 1)));
            timelineProgressBar.play();
            timelineProgressBar.setOnFinished(actionEvent -> setSelected(true));
        };

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterEvent);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterEvent);

        EventHandler<Event> exitEvent = e -> {
            Point2D mouse;
            if (e.getEventType() == GazeEvent.GAZE_EXITED) {
                double offsetX = 0, offsetY = 0;
                Scene scene = gameContext.getPrimaryStage().getScene();
                if (scene != null) {
                    Window window = scene.getWindow();
                    offsetX = window.getX() + scene.getX();
                    offsetY = window.getY() + scene.getY();
                }
                mouse = this.screenToLocal((int)((GazeEvent)e).getX() +offsetX ,(int)((GazeEvent)e).getY() +offsetY);
            } else {
                mouse = new Point2D((int)((MouseEvent)e).getX(),(int)((MouseEvent)e).getY());
            }
                int way = checkPos(mouse);
                if (selected && !endOfGame && !intersect && !onMouse(mouse)) {
                    moveTo(way);
                }
                intersect = false;

                if (!selected) {
                    timelineProgressBar.stop();
                    pi.setProgress(0);
                    pi.setOpacity(0);
                }
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitEvent);

        this.setToX(carX);
        this.setToY(carY);

    }

    public void update(int size) {
        this.size = size;
        this.setWidth(l * size);
        this.setHeight(h * size);
        this.setToX(x);
        this.setToY(y);
    }

    private int checkPos(Point2D mouse) {
        if (orientation == Orientation.vertical && (this.getX() < mouse.getX()) && (mouse.getX() < this.getX() + this.getWidth())) {
            return (this.getY() > mouse.getY()) ? -1 : 1;
        } else if (orientation == Orientation.horizontal && (this.getY() < mouse.getY()) && (mouse.getY() < this.getY() + this.getHeight())) {
            return (this.getX() > mouse.getX()) ? -1 : 1;
        }

        setSelected(false);
        timelineProgressBar.stop();
        pi.setProgress(0);
        pi.setOpacity(0);
        return 0;
    }

    private void moveTo(int way) {
        double saveX = this.getX();
        double saveY = this.getY();
        int prevx = x;
        int prevy = y;
        if (orientation == Orientation.horizontal) {
            this.setX(saveX + way * size);
            x = x + way;
        } else {
            this.setY(saveY + way * size);
            y = y + way;
        }
        if (intersect) {
            this.setX(saveX);
            this.setY(saveY);
            x = prevx;
            y = prevy;
        }
    }

    private boolean onMouse(Point2D mouse) {
        return (this.getX() < mouse.getX()) && (mouse.getX() < this.getX() + this.getWidth())
            && (this.getY() < mouse.getY()) && (mouse.getY() < this.getY() + this.getHeight());
    }

    public void setToX(int i) {
        super.setX(size + i * size);
        x = i;
    }

    public void setToY(int i) {
        super.setY(size + i * size);
        y = i;
    }

    public void setSelected(boolean b) {
        selected = b;

        if (selected) {
            this.setOpacity(1);

        } else {
            this.setOpacity(0.7);

        }
    }

}
