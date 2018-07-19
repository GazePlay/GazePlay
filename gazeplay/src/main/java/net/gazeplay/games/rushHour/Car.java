package net.gazeplay.games.rushHour;

import java.awt.MouseInfo;
import java.awt.Point;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.DefaultGamesLocator;
import net.gazeplay.GameContext;

@Slf4j
public class Car extends Rectangle {

    private boolean selected;

    @Getter
    private boolean direction;
    private int size;
    private int move = 0;
    @Setter
    private boolean intersect = false;
    GameContext gameContext;

    Car(int l, int h, Color c, boolean direction, int size, GameContext gameContext) {
        super(0, 0, l * size, h * size);
        this.setFill(c);
        this.gameContext = gameContext;
        this.direction = direction;
        this.size = size;

        EventHandler<Event> enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                selected = true;
                log.info("selected");
            }
        };

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterEvent);

        EventHandler<Event> exitEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                boolean out = false;

                Point mouse = MouseInfo.getPointerInfo().getLocation();

                if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
                    int way = checkPos(mouse);
                    selected = false;
                    log.info("unselected");

                    if (!out && !intersect && !onMouse(mouse)) {
                        // moveToMouse(mouse);
                        moveTo(way, mouse);
                        selected = false;
                    }
                    intersect = false;
                }
            }
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);

    }

    private int checkPos(Point mouse) {
        Point2D coord = this.sceneToLocal(
                mouse.getX() - gameContext.getGazePlay().getPrimaryScene().getX()
                        - gameContext.getGazePlay().getPrimaryStage().getX(),
                mouse.getY() - gameContext.getGazePlay().getPrimaryScene().getY()
                        - gameContext.getGazePlay().getPrimaryStage().getY());

        if (!direction && (this.getX() < coord.getX()) && (coord.getX() < this.getX() + this.getWidth())) {
            return (this.getY() > coord.getY()) ? -1 : 1;
        } else if (direction && (this.getY() < coord.getY()) && (coord.getY() < this.getY() + this.getHeight())) {
            return (this.getX() > coord.getX()) ? -1 : 1;
        }

        log.info("car (x;y) = (" + this.getX() + ";" + this.getY() + ")");
        log.info("car offset (x;y) = (" + (this.getX() + this.getWidth()) + ";" + (this.getY() + this.getHeight())
                + ")");
        log.info("mouse (x;y) = (" + coord.getX() + ";" + coord.getY() + ")");

        return 0;
    }

    private void moveTo(int way, Point mouse) {
        Point2D coord = this.sceneToLocal(
                mouse.getX() - gameContext.getGazePlay().getPrimaryScene().getX()
                        - gameContext.getGazePlay().getPrimaryStage().getX(),
                mouse.getY() - gameContext.getGazePlay().getPrimaryScene().getY()
                        - gameContext.getGazePlay().getPrimaryStage().getY());
        double saveX = this.getX();
        double saveY = this.getY();
        if (this.isDirection()) {
            this.setX(this.getX() + way * size);
        } else {
            this.setY(this.getY() + way * size);
        }
        if (intersect) {
            this.setX(saveX);
            this.setY(saveY);
        }
    }

    private void moveToMouse(Point mouse) {
        Point2D coord = this.sceneToLocal(
                mouse.getX() - gameContext.getGazePlay().getPrimaryScene().getX()
                        - gameContext.getGazePlay().getPrimaryStage().getX(),
                mouse.getY() - gameContext.getGazePlay().getPrimaryScene().getY()
                        - gameContext.getGazePlay().getPrimaryStage().getY());
        double saveX = this.getX();
        double saveY = this.getY();
        if (this.isDirection()) {
            this.setX(((int) coord.getX() / size) * size);
        } else {
            this.setY(((int) coord.getY() / size) * size);
        }
        if (intersect) {
            this.setX(saveX);
            this.setY(saveY);
        }
    }

    private boolean onMouse(Point mouse) {
        Point2D coord = this.sceneToLocal(
                mouse.getX() - gameContext.getGazePlay().getPrimaryScene().getX()
                        - gameContext.getGazePlay().getPrimaryStage().getX(),
                mouse.getY() - gameContext.getGazePlay().getPrimaryScene().getY()
                        - gameContext.getGazePlay().getPrimaryStage().getY());
        return (this.getX() < coord.getX()) && (coord.getX() < this.getX() + this.getWidth())
                && (this.getY() < coord.getY()) && (coord.getY() < this.getY() + this.getHeight());
    }

    public void setToX(int i) {
        super.setX(size + i * size);
    }

    public void setToY(int i) {
        super.setY(size + i * size);
    }

}