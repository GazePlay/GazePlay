package net.gazeplay.games.rushHour;

import java.util.LinkedList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.DefaultGamesLocator;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;

@Slf4j
public class RushHour extends Parent implements GameLifeCycle {

    public GameContext gameContext;
    public int size = 100;
    public Rectangle ground;

    public Car toWin;

    public List<Car> garage;
    public Shape walls;

    public RushHour(GameContext gameContext) {
        this.gameContext = gameContext;
        setLevel(0);

    }

    public void setLevel(int i) {

        garage = new LinkedList<Car>();

        if (i == 0) {
            setLevel0();
        }

        setIntersections();

    }

    public void setLevel0() {
        Car red = new Car(2, 1, Color.RED, true, size, gameContext);
        red.setToX(1);
        red.setToY(2);
        garage.add(red);
        gameContext.getChildren().add(red);

        toWin = red;

        Car blue = new Car(1, 3, Color.BLUE, false, size, gameContext);
        blue.setToX(3);
        blue.setToY(1);
        garage.add(blue);
        gameContext.getChildren().add(blue);

        Car vert = new Car(2, 1, Color.GREEN, true, size, gameContext);
        vert.setToX(1);
        vert.setToY(0);
        garage.add(vert);
        gameContext.getChildren().add(vert);

        Car purple = new Car(1, 3, Color.PURPLE, false, size, gameContext);
        purple.setToX(0);
        purple.setToY(0);
        garage.add(purple);
        gameContext.getChildren().add(purple);

        Car orange = new Car(1, 2, Color.ORANGE, false, size, gameContext);
        orange.setToX(0);
        orange.setToY(3);
        garage.add(orange);
        gameContext.getChildren().add(orange);

        Car lightBlue = new Car(2, 1, Color.LIGHTBLUE, true, size, gameContext);
        lightBlue.setToX(1);
        lightBlue.setToY(4);
        garage.add(lightBlue);
        gameContext.getChildren().add(lightBlue);

        Car lightGreen = new Car(3, 1, Color.LIGHTGREEN, true, size, gameContext);
        lightGreen.setToX(2);
        lightGreen.setToY(5);
        garage.add(lightGreen);
        gameContext.getChildren().add(lightGreen);

        Car yellow = new Car(1, 3, Color.YELLOW, false, size, gameContext);
        yellow.setToX(5);
        yellow.setToY(3);
        garage.add(yellow);
        gameContext.getChildren().add(yellow);

        createGarage(6, 6, new Rectangle((6 + 1) * size, ((6 / 2)) * size, size, size));

        toWinListener();

    }

    @Override
    public void launch() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void toWinListener() {
        toWin.xProperty().addListener((o) -> {
            if (Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent actionEvent) {

                        log.info("you won !");
                    }
                });
            }
        });

        toWin.yProperty().addListener((o) -> {
            if (Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent actionEvent) {

                        log.info("you won !");
                    }
                });
            }
        });
    }

    public void setIntersections() {
        for (Car car : garage) {
            if (car.isDirection()) {
                car.xProperty().addListener((o) -> {
                    checkIntersections(car);
                });
            } else {
                car.yProperty().addListener((o) -> {
                    checkIntersections(car);
                });
            }
        }
    }

    public void checkIntersections(Car car) {
        for (Car car2 : garage) {
            if (car2 != car) {
                if (Shape.intersect(car, car2).getBoundsInLocal().getWidth() != -1) {
                    log.info("intersect");
                    car.setIntersect(true);
                }
            }
        }
        if (Shape.intersect(car, walls).getBoundsInLocal().getWidth() != -1) {
            log.info("intersect");
            car.setIntersect(true);
        }

    }

    public void createGarage(int longueur, int hauteur, Rectangle givenDoor) {
        Rectangle up = new Rectangle(0, 0, (longueur + 2) * size, size);
        Rectangle down = new Rectangle(0, (hauteur + 1) * size, (longueur + 2) * size, size);
        Rectangle left = new Rectangle(0, 0, size, (hauteur + 2) * size);
        Rectangle right = new Rectangle((longueur + 1) * size, 0, size, (hauteur + 2) * size);

        Shape upDown = Shape.union(up, down);
        Shape leftRight = Shape.union(left, right);
        Shape sides = Shape.union(upDown, leftRight);

        Rectangle door = givenDoor;

        walls = Shape.subtract(sides, door);
        walls.setFill(Color.WHITE);

        ground = new Rectangle(0, 0, size * (longueur + 2), size * (hauteur + 2));
        ground.setFill(Color.SLATEGRAY);

        gameContext.getChildren().add(ground);

        ground.toBack();

        gameContext.getChildren().add(walls);
    }

}
