package net.gazeplay.games.rushhour;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.awt.*;

@Slf4j
public class Car extends Rectangle {

    @Setter
    private boolean endOfGame = false;

    @Getter
    private final boolean direction;
    private int size;
    private int move = 0;
    private int x;
    private int y;
    private final int l;
    private final int h;

    @Setter
    private boolean intersect = false;
    private Timeline timelineProgressBar;
    final IGameContext gameContext;
    private final ProgressIndicator pi;

    private boolean selected = false;
    private boolean timerRunning = false;


    /**
     * Creates a new instance of a Car with the size and direction.
     *
     * @param l         length of the car
     * @param h         height of the car
     * @param direction of the car, true for horizontal, false for vertical
     * @param size,     the size of one block
     */
    Car(final int x, final int y, final int l, final int h, final Color c, final boolean direction, final int size, final ProgressIndicator pi,
        final IGameContext gameContext) {
        super(0, 0, l * size, h * size);
        if (direction) {
            this.setFill(new ImagePattern(new Image("data/rushHour/carH.png")));
        } else {
            this.setFill(new ImagePattern(new Image("data/rushHour/carV.png")));
        }

        final Lighting lighting = new Lighting();
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

        final EventHandler<Event> enterEvent = e -> {
            pi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
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
            timerRunning = true;
        };

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterEvent);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterEvent);


        final EventHandler<Event> exitEvent = e -> {
            if (timerRunning) {
                timelineProgressBar.stop();
                pi.setProgress(0);
                timerRunning = false;
            }
            Point pointerPosition = new Point();;
            if (e.getEventType() == GazeEvent.GAZE_EXITED) {
                pointerPosition.setLocation(((GazeEvent)e).getX(), ((GazeEvent)e).getY());
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
                pointerPosition.setLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
            }

            final int way = checkPos(pointerPosition);
            if (selected && !endOfGame && !intersect && !onMouse(pointerPosition)) {
                moveTo(way);
            }
            intersect = false;

            if (!selected) {
                pi.setOpacity(0);
            }
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitEvent);

        this.setToX(x);
        this.setToY(y);

        gameContext.getGazeDeviceManager().addEventFilter(this);
    }

    public void update(final int size) {
        this.size = size;
        this.setWidth(l * size);
        this.setHeight(h * size);
        this.setToX(x);
        this.setToY(y);
    }

    private int checkPos(final Point pointerCoordinates) {

        if (!direction && (this.getX() < pointerCoordinates.getX()) && (pointerCoordinates.getX() < this.getX() + this.getWidth())) {
            return (this.getY() > pointerCoordinates.getY()) ? -1 : 1;
        } else if (direction && (this.getY() < pointerCoordinates.getY()) && (pointerCoordinates.getY() < this.getY() + this.getHeight())) {
            return (this.getX() > pointerCoordinates.getX()) ? -1 : 1;
        }

        setSelected(false);
        timelineProgressBar.stop();
        pi.setProgress(0);
        pi.setOpacity(0);
        return 0;
    }

    private void moveTo(final int way) {
        final double saveX = this.getX();
        final double saveY = this.getY();
        final int prevx = x;
        final int prevy = y;
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

    private boolean onMouse(final Point mousePosition) {
        final Point2D coord = this.sceneToLocal(
            mousePosition.getX() - gameContext.getPrimaryScene().getX()
                - gameContext.getPrimaryStage().getX(),
            mousePosition.getY() - gameContext.getPrimaryScene().getY()
                - gameContext.getPrimaryStage().getY());
        return (this.getX() < coord.getX()) && (coord.getX() < this.getX() + this.getWidth())
            && (this.getY() < coord.getY()) && (coord.getY() < this.getY() + this.getHeight());
    }

    public void setToX(final int i) {
        super.setX(size + i * size);
        x = i;
    }

    public void setToY(final int i) {
        super.setY(size + i * size);
        y = i;
    }

    public void setSelected(final boolean b) {
        selected = b;

        if (selected) {
            this.setOpacity(1);

        } else {
            this.setOpacity(0.7);

        }
    }

}
