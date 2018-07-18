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
    GameContext gameContext;

    Car(int l, int h, Color c, boolean direction, Pane city, int size, GameContext gameContext) {
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
                if (selected) {
                    if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
                        Point mouse = MouseInfo.getPointerInfo().getLocation();
                        int way;
                        if (0 != (way = checkPos(mouse))) {
                            moveTo(city, way);
                        }
                        selected = false;
                        log.info("unselected");
                    }
                }
            }
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);

    }

    private Car getInstance() {
        return this;
    }

    private void checkWin() {

    }

    private int checkPos(Point mouse) {
        Point2D coord = this.sceneToLocal(mouse.getX() - gameContext.getGazePlay().getPrimaryStage().getX(),
                mouse.getY() - gameContext.getGazePlay().getPrimaryStage().getY());

        if (!direction && (this.getX() <= coord.getX()) && (coord.getX() <= this.getX() + this.getWidth())) {
            return (this.getY() < coord.getY()) ? 1 : -1;
        } else if (direction && (this.getY() <= coord.getY()) && (coord.getY() <= this.getY() + this.getHeight())) {
            return (this.getX() < coord.getX()) ? 1 : -1;
        }

        return 0;
    }

    private boolean isInside(Point mouse) {
        Point2D coord = this.sceneToLocal(mouse.getX() - gameContext.getGazePlay().getPrimaryStage().getX(),
                mouse.getY() - gameContext.getGazePlay().getPrimaryStage().getY());
        boolean b = (this.getX() - 5 <= coord.getX()) && (coord.getX() <= 5 + this.getX() + this.getWidth())
                && (this.getY() - 5 <= coord.getY()) && (coord.getY() <= 5 + this.getY() + this.getHeight());
        log.info("" + b);
        return b;
    }

    private void moveTo(Pane city, int way) {
        if (this.isDirection()) {
            this.setX(this.getX() + size);
        } else {
            this.setY(this.getY() + size);
        }
    }

}