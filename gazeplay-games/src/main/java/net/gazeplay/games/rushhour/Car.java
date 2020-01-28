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
    private boolean direction;
    private int size;
    private int move = 0;
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

    private double offsetX, offsetY;

    /**
     * Creates a new instance of a Car with the size and direction.
     * 
     * @param l
     *            length of the car
     * @param h
     *            height of the car
     * @param direction
     *            of the car, true for horizontal, false for vertical
     * @param size,
     *            the size of one block
     */
    Car(int x, int y, int l, int h, Color c, boolean direction, int size, ProgressIndicator pi,
            IGameContext gameContext) {
        super(0, 0, l * size, h * size);
        if (direction) {
            this.setFill(new ImagePattern(new Image("data/rushHour/carH.png")));
        } else {
            this.setFill(new ImagePattern(new Image("data/rushHour/carV.png")));
        }

        Scene scene =  gameContext.getPrimaryStage().getScene();
        if (scene != null) {
            Window window = scene.getWindow();
            offsetX = window.getX();
            offsetY = window.getY();
        } else {
            offsetX = 0;
            offsetY = 0;
        }

        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(100.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(30, 30, c));

        this.setEffect(lighting);
        this.gameContext = gameContext;
        this.direction = direction;
        this.size = size;
        this.setOpacity(0.7);
        this.pi = pi;
        this.l = l;
        this.h = h;
        this.x = x;
        this.y = y;

        log.debug("" + x + " " + y + " " + l + " " + h + " " + c);

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
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            if (e.getEventType() == GazeEvent.GAZE_EXITED) {
                Screen mainScreen = Screen.getMainScreen();

                double screenWidth = mainScreen.getWidth();
                double screenHeight = mainScreen.getHeight();

                float[] pointAsFloatArray = Tobii.gazePosition();

                final float xRatio = pointAsFloatArray[0];
                final float yRatio = pointAsFloatArray[1];

                final double positionX = xRatio * screenWidth;
                final double positionY = yRatio * screenHeight;

                mouse = new Point();
                mouse.setLocation(positionX + offsetX, positionY + offsetY);
            }

            int way = checkPos(mouse);
            if (selected && !endOfGame && !intersect && !onMouse(mouse)) {
                // moveToMouse(mouse);
                moveTo(way, mouse);
            }
            intersect = false;
            // }

            if (!selected) {
                timelineProgressBar.stop();
                pi.setProgress(0);
                pi.setOpacity(0);
            }
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitEvent);

        this.setToX(x);
        this.setToY(y);

    }

    public void update(int size) {
        this.size = size;
        this.setWidth(l * size);
        this.setHeight(h * size);
        this.setToX(x);
        this.setToY(y);
    }

    private int checkPos(Point mouse) {
        Point2D coord = this.sceneToLocal(
                mouse.getX() - gameContext.getPrimaryScene().getX()
                        - gameContext.getPrimaryStage().getX(),
                mouse.getY() - gameContext.getPrimaryScene().getY()
                        - gameContext.getPrimaryStage().getY());

        if (!direction && (this.getX() < coord.getX()) && (coord.getX() < this.getX() + this.getWidth())) {
            return (this.getY() > coord.getY()) ? -1 : 1;
        } else if (direction && (this.getY() < coord.getY()) && (coord.getY() < this.getY() + this.getHeight())) {
            return (this.getX() > coord.getX()) ? -1 : 1;
        }

        setSelected(false);
        timelineProgressBar.stop();
        pi.setProgress(0);
        pi.setOpacity(0);
        return 0;
    }

    private void moveTo(int way, Point mouse) {
        double saveX = this.getX();
        double saveY = this.getY();
        int prevx = x;
        int prevy = y;
        if (this.isDirection()) {
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

    private boolean onMouse(Point mouse) {
        Point2D coord = this.sceneToLocal(
                mouse.getX() - gameContext.getPrimaryScene().getX()
                        - gameContext.getPrimaryStage().getX(),
                mouse.getY() - gameContext.getPrimaryScene().getY()
                        - gameContext.getPrimaryStage().getY());
        return (this.getX() < coord.getX()) && (coord.getX() < this.getX() + this.getWidth())
                && (this.getY() < coord.getY()) && (coord.getY() < this.getY() + this.getHeight());
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
