package net.gazeplay.games.rushHour;

import java.util.LinkedList;
import java.util.List;

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

    public List<Car> garage;
    public Shape walls;

    public RushHour(GameContext gameContext) {
        this.gameContext = gameContext;
        setLevel(0);

    }

    public void setLevel(int i) {

        garage = new LinkedList<Car>();

        Car red = new Car(1, 2, Color.RED, true, size, gameContext);
        red.setToX(0);
        red.setToY(0);
        garage.add(red);
        gameContext.getChildren().add(red);

        Car blue = new Car(2, 2, Color.BLUE, false, size, gameContext);
        blue.setToX(4);
        blue.setToY(5);
        garage.add(blue);
        gameContext.getChildren().add(blue);

        createGarage(10, 6);

        setIntersections();

    }

    @Override
    public void launch() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

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

    public void createGarage(int longueur, int hauteur) {
        Rectangle up = new Rectangle(0, 0, (longueur + 2) * size, size);
        Rectangle down = new Rectangle(0, (hauteur + 1) * size, (longueur + 2) * size, size);
        Rectangle left = new Rectangle(0, 0, size, (hauteur + 2) * size);
        Rectangle right = new Rectangle((longueur + 1) * size, 0, size, (hauteur + 2) * size);

        Shape upDown = Shape.union(up, down);
        Shape leftRight = Shape.union(left, right);
        Shape sides = Shape.union(upDown, leftRight);

        Rectangle door = new Rectangle((longueur + 1) * size, ((hauteur / 2)) * size, size, size * 2);

        walls = Shape.subtract(sides, door);
        walls.setFill(Color.WHITE);
        gameContext.getChildren().add(walls);
    }

}
