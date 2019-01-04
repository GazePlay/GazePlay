package net.gazeplay.games.rushHour;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.DefaultGamesLocator;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;

@Slf4j
public class RushHour extends Parent implements GameLifeCycle {

    public GameContext gameContext;
    public IntegerProperty size;
    public Rectangle ground;
    public boolean endOfGame = false;

    public int garageHeight;
    public int garageWidth;
    private Pane p;

    Rectangle up;
    Rectangle down;
    Rectangle left;
    Rectangle right;
    Rectangle door;

    public Car toWin;

    public List<Car> garage;

    int level;
    int numberLevels = 2;

    public RushHour(GameContext gameContext) {
        this.gameContext = gameContext;
        level = 0;
        size = new SimpleIntegerProperty();
        gameContext.getGazePlay().getPrimaryStage().widthProperty().addListener((observable, oldValue, newValue) -> {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
                    ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        });
        gameContext.getGazePlay().getPrimaryStage().heightProperty().addListener((observable, oldValue, newValue) -> {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
                    ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        });
    }

    public void setLevel(int i) {

        garage = new LinkedList<Car>();

        size = new SimpleIntegerProperty();

        ProgressIndicator pi = new ProgressIndicator(0);
        pi.setMouseTransparent(true);
        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            pi.setPrefSize(newValue.intValue(), newValue.intValue());
            for (Car car : garage) {
                car.update(newValue.intValue());
            }

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            ground.setHeight(newValue.intValue() * (garageHeight + 2));
            ground.setWidth(newValue.intValue() * (garageWidth + 2));

            p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth() / 2);
            p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight() / 2);
        });

        p = new Pane();

        p.getChildren().add(pi);

        if (i == 0) {
            setLevel0(p, pi);
        } else if (i == 1) {
            setLevel1(p, pi);
        }

        toWinListener();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth() / 2);
        p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight() / 2);

        gameContext.getChildren().add(p);
        gameContext.getGazeDeviceManager().addEventFilter(p);

        setIntersections();

    }

    public void setLevel1(Pane p, ProgressIndicator pi) {

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
                ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2)) * size.getValue(),
                size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        Car blue = new Car(5, 1, 1, 3, Color.BLUE, false, size.getValue(), pi, gameContext);
        garage.add(blue);
        p.getChildren().add(blue);

        Car vert = new Car(4, 0, 2, 1, Color.GREEN, true, size.getValue(), pi, gameContext);
        garage.add(vert);
        p.getChildren().add(vert);

        Car purple = new Car(0, 3, 3, 1, Color.PURPLE, true, size.getValue(), pi, gameContext);
        garage.add(purple);
        p.getChildren().add(purple);

        Car yellow = new Car(2, 0, 1, 3, Color.YELLOW, false, size.getValue(), pi, gameContext);
        garage.add(yellow);
        p.getChildren().add(yellow);

    }

    public void setLevel0(Pane p, ProgressIndicator pi) {

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
                ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2)) * size.getValue(),
                size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        Car red = new Car(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        Car blue = new Car(3, 1, 1, 3, Color.BLUE, false, size.getValue(), pi, gameContext);
        garage.add(blue);
        p.getChildren().add(blue);

        Car vert = new Car(1, 0, 2, 1, Color.GREEN, true, size.getValue(), pi, gameContext);
        garage.add(vert);
        p.getChildren().add(vert);

        Car purple = new Car(0, 0, 1, 3, Color.PURPLE, false, size.getValue(), pi, gameContext);
        garage.add(purple);
        p.getChildren().add(purple);

        Car orange = new Car(0, 3, 1, 2, Color.ORANGE, false, size.getValue(), pi, gameContext);
        garage.add(orange);
        p.getChildren().add(orange);

        Car lightBlue = new Car(1, 4, 2, 1, Color.LIGHTBLUE, true, size.getValue(), pi, gameContext);
        garage.add(lightBlue);
        p.getChildren().add(lightBlue);

        Car lightGreen = new Car(2, 5, 3, 1, Color.LIGHTGREEN, true, size.getValue(), pi, gameContext);
        garage.add(lightGreen);
        p.getChildren().add(lightGreen);

        Car yellow = new Car(5, 3, 1, 3, Color.YELLOW, false, size.getValue(), pi, gameContext);
        garage.add(yellow);
        p.getChildren().add(yellow);

    }

    @Override
    public void launch() {
        endOfGame = false;
        setLevel(level);
        if (toWin.isDirection()) {
            toWin.setFill(new ImagePattern(new Image("data/rushHour/taxiH.png")));
        } else {
            toWin.setFill(new ImagePattern(new Image("data/rushHour/taxiV.png")));
        }
        toWin.setEffect(null);
        level = (level + 1) % numberLevels;
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

                        log.debug("you won !");
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
                    log.debug("intersect");
                    car.setIntersect(true);
                    car.setSelected(false);
                }
            }
        }
        if (Shape.intersect(car, door).getBoundsInLocal().getWidth() != -1) {
            // do nothing
        } else if ((Shape.intersect(car, up).getBoundsInLocal().getWidth() != -1)
                || (Shape.intersect(car, down).getBoundsInLocal().getWidth() != -1)
                || (Shape.intersect(car, left).getBoundsInLocal().getWidth() != -1)
                || (Shape.intersect(car, right).getBoundsInLocal().getWidth() != -1)) {
            log.debug("intersect");
            car.setIntersect(true);
            car.setSelected(false);
        }

    }

    public void createGarage(Pane p) {
        int longueur = garageWidth;
        int hauteur = garageHeight;

        up = new Rectangle(0, 0, (longueur + 2) * size.getValue(), size.getValue());
        down = new Rectangle(0, (hauteur + 1) * size.getValue(), (longueur + 2) * size.getValue(), size.getValue());
        left = new Rectangle(0, 0, size.getValue(), (hauteur + 2) * size.getValue());
        right = new Rectangle((longueur + 1) * size.getValue(), 0, size.getValue(), (hauteur + 2) * size.getValue());
        up.setFill(Color.WHITE);
        down.setFill(Color.WHITE);
        left.setFill(Color.WHITE);
        right.setFill(Color.WHITE);

        door.setFill(Color.SLATEGRAY);

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            up.setWidth((longueur + 2) * newValue.intValue());
            up.setHeight(newValue.intValue());
            down.setWidth((longueur + 2) * newValue.intValue());
            down.setHeight(newValue.intValue());
            down.setY((hauteur + 1) * newValue.intValue());
            left.setWidth(size.getValue());
            left.setHeight((hauteur + 2) * newValue.intValue());
            right.setWidth(newValue.intValue());
            right.setHeight((hauteur + 2) * newValue.intValue());
            right.setX((longueur + 1) * newValue.intValue());
        });

        ground = new Rectangle(0, 0, size.getValue() * (longueur + 2), size.getValue() * (hauteur + 2));
        ground.setFill(Color.SLATEGRAY);

        p.getChildren().add(ground);

        ground.toBack();

        p.getChildren().addAll(up, down, left, right, door);
    }

}
