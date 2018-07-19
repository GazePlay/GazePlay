package net.gazeplay.games.rushHour;

import java.util.LinkedList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
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
    public boolean endOfGame = false;

    public Car toWin;

    public List<Car> garage;
    public Shape walls;

    public RushHour(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void setLevel(int i) {

        garage = new LinkedList<Car>();

        if (i == 0) {
            setLevel0();
        }

        setIntersections();

    }

    public void setLevel0() {

        ProgressIndicator pi = new ProgressIndicator(0);
        pi.setPrefSize(size, size);
        pi.setMouseTransparent(true);

        Pane p = new Pane();

        p.getChildren().add(pi);

        Car red = new Car(2, 1, Color.RED, true, size, pi, gameContext);
        red.setToX(1);
        red.setToY(2);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        Car blue = new Car(1, 3, Color.BLUE, false, size, pi, gameContext);
        blue.setToX(3);
        blue.setToY(1);
        garage.add(blue);
        p.getChildren().add(blue);

        Car vert = new Car(2, 1, Color.GREEN, true, size, pi, gameContext);
        vert.setToX(1);
        vert.setToY(0);
        garage.add(vert);
        p.getChildren().add(vert);

        Car purple = new Car(1, 3, Color.PURPLE, false, size, pi, gameContext);
        purple.setToX(0);
        purple.setToY(0);
        garage.add(purple);
        p.getChildren().add(purple);

        Car orange = new Car(1, 2, Color.ORANGE, false, size, pi, gameContext);
        orange.setToX(0);
        orange.setToY(3);
        garage.add(orange);
        p.getChildren().add(orange);

        Car lightBlue = new Car(2, 1, Color.LIGHTBLUE, true, size, pi, gameContext);
        lightBlue.setToX(1);
        lightBlue.setToY(4);
        garage.add(lightBlue);
        p.getChildren().add(lightBlue);

        Car lightGreen = new Car(3, 1, Color.LIGHTGREEN, true, size, pi, gameContext);
        lightGreen.setToX(2);
        lightGreen.setToY(5);
        garage.add(lightGreen);
        p.getChildren().add(lightGreen);

        Car yellow = new Car(1, 3, Color.YELLOW, false, size, pi, gameContext);
        yellow.setToX(5);
        yellow.setToY(3);
        garage.add(yellow);
        p.getChildren().add(yellow);

        createGarage(6, 6, new Rectangle((6 + 1) * size, ((6 / 2)) * size, size, size), p);

        toWinListener();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth() / 2);
        p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight() / 2);

        gameContext.getChildren().add(p);

    }

    @Override
    public void launch() {
        endOfGame = false;
        setLevel(0);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    public void toWinListener() {
        toWin.xProperty().addListener((o) -> {
            if (!endOfGame && Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                endOfGame = true;
                gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent actionEvent) {

                        log.info("you won !");
                        dispose();
                        launch();
                    }
                });
            }
        });

        toWin.yProperty().addListener((o) -> {
            if (!endOfGame && Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                endOfGame = true;
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
                    car.setSelected(false);
                }
            }
        }
        if (Shape.intersect(car, walls).getBoundsInLocal().getWidth() != -1) {
            log.info("intersect");
            car.setIntersect(true);
            car.setSelected(false);
        }

    }

    public void createGarage(int longueur, int hauteur, Rectangle givenDoor, Pane p) {

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

        p.getChildren().add(ground);

        ground.toBack();

        p.getChildren().add(walls);
    }

}
