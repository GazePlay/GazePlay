package net.gazeplay.games.rushhour;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class RushHour extends Parent implements GameLifeCycle {

    public final IGameContext gameContext;
    public IntegerProperty size;
    private Rectangle ground;
    private boolean endOfGame = false;

    private int garageHeight;
    private int garageWidth;
    private Pane p;

    private Rectangle up;
    private Rectangle down;
    private Rectangle left;
    private Rectangle right;
    private Rectangle door;

    private Car toWin;

    private List<Car> garage;

    private int level;

    public RushHour(final IGameContext gameContext) {
        this.gameContext = gameContext;
        level = 0;
        size = new SimpleIntegerProperty();
        gameContext.getPrimaryStage().widthProperty().addListener((observable, oldValue, newValue) -> {

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
                ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        });
        gameContext.getPrimaryStage().heightProperty().addListener((observable, oldValue, newValue) -> {

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
                ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        });

        ground = new Rectangle(); // to avoid NullPointerException

    }

    private void setLevel(final int i) {

        garage = new LinkedList<>();

        size = new SimpleIntegerProperty();

        final ProgressIndicator pi = new ProgressIndicator(0);
        pi.setMouseTransparent(true);
        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            pi.setPrefSize(newValue.intValue(), newValue.intValue());
            for (final Car car : garage) {
                car.update(newValue.intValue());
            }

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            ground.setHeight(newValue.intValue() * (garageHeight + 2));
            ground.setWidth(newValue.intValue() * (garageWidth + 2));

            p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth()/ 2d);
            p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight()/ 2d);
        });

        p = new Pane();

        p.getChildren().add(pi);

        if (i == 0) {
            setLevel0(p, pi);
        } else if (i == 1) {
            setLevel1(p, pi);
        } else if (i == 2) {
            setLevel2(p, pi);
        } else if (i == 3) {
            setLevel3(p, pi);
        } else if (i == 4) {
            setLevel4(p, pi);
        } else if (i == 5) {
            setLevel5(p, pi);
        } else if (i == 6) {
            setLevel6(p, pi);
        } else if (i == 7) {
            setLevel7(p, pi);
        } else if (i == 8) {
            setLevel8(p, pi);
        } else if (i == 9) {
            setLevel9(p, pi);
        } else if (i == 10) {
            setLevel10(p, pi);
        } else if (i == 11) {
            setLevel11(p, pi);
        } else if (i == 12) {
            setLevel12(p, pi);
        } else if (i == 13) {
            setLevel13(p, pi);
        } else if (i == 14) {
            setLevel14(p, pi);
        } else if (i == 15) {
            setLevel15(p, pi);
        } else if (i == 16) {
            setLevel16(p, pi);
        } else if (i == 17) {
            setLevel17(p, pi);
        } else if (i == 18) {
            setLevel18(p, pi);
        } else if (i == 19) {
            setLevel19(p, pi);
        } else if (i == 20) {
            setLevel20(p, pi);
        } else if (i == 21) {
            setLevel21(p, pi);
        } else if (i == 22) {
            setLevel22(p, pi);
        } else if (i == 23) {
            setLevel23(p, pi);
        } else if (i == 24) {
            setLevel24(p, pi);
        } else if (i == 25) {
            setLevel25(p, pi);
        } else if (i == 26) {
            setLevel26(p, pi);
        } else if (i == 27) {
            setLevel27(p, pi);
        } else if (i == 28) {
            setLevel28(p, pi);
        } else if (i == 29) {
            setLevel29(p, pi);
        } else if (i == 30) {
            setLevel30(p, pi);
        } else if (i == 31) {
            setLevel31(p, pi);
        } else if (i == 32) {
            setLevel32(p, pi);
        }

        toWinListener();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth()/ 2d);
        p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight()/ 2d);

        gameContext.getChildren().add(p);
        gameContext.getGazeDeviceManager().addEventFilter(p);

        setIntersections();

    }

    private void setLevel0(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(0, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(1, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

    }

    private void setLevel1(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(0, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(5, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

    }

    public void setLevel2(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c7 = new Car(2, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

    }

    public void setLevel3(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(0, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(1, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(2, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(5, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

    }

    public void setLevel4(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight / 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight / 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(1, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(3, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(3, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);
    }

    public void setLevel5(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(1, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(4, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(5, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(4, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(3, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

    }

    public void setLevel6(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(5, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel7(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(1, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(2, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(3, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(4, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(2, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel8(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(0, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(2, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

    }

    public void setLevel9(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(1, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(3, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel10(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(4, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(2, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(1, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(2, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel11(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(5, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel12(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(1, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel13(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(4, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel14(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(2, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

        final Car c12 = new Car(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c12);
        p.getChildren().add(c12);

    }

    public void setLevel15(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(2, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(1, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(0, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

        final Car c12 = new Car(1, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c12);
        p.getChildren().add(c12);

    }

    public void setLevel16(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(2, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(1, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel17(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(1, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel18(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(4, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel19(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

        final Car c12 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c12);
        p.getChildren().add(c12);

        final Car c13 = new Car(3, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c13);
        p.getChildren().add(c13);

        final Car c14 = new Car(3, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c14);
        p.getChildren().add(c14);

    }

    public void setLevel20(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(4, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(5, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel21(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(5, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(0, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(1, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(2, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(2, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(1, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(3, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

    }

    public void setLevel22(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(1, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel23(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(5, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(3, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel24(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(3, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(2, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel25(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(2, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel26(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(0, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(0, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

    }

    public void setLevel27(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(1, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(0, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

    }

    public void setLevel28(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

    }

    public void setLevel29(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(5, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(3, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

    }

    public void setLevel30(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(0, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    public void setLevel31(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(0, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(2, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

        final Car c11 = new Car(3, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c11);
        p.getChildren().add(c11);

        final Car c12 = new Car(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c12);
        p.getChildren().add(c12);

    }

    public void setLevel32(final Pane p, final ProgressIndicator pi) {

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        garageWidth = 6;
        garageHeight = 6;

        size.set((int) ((dimension2D.getWidth() > dimension2D.getHeight())
            ? dimension2D.getHeight() / (garageHeight + 2) : dimension2D.getWidth() / (garageWidth + 2)));

        door = new Rectangle((garageWidth + 1) * size.getValue(), ((garageHeight/ 2d)) * size.getValue(),
            size.getValue(), size.getValue());

        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            door.setX((garageWidth + 1) * newValue.intValue());
            door.setY(((garageHeight/ 2d)) * newValue.intValue());
            door.setWidth(newValue.intValue());
            door.setHeight(newValue.intValue());
        });

        createGarage(p);

        final Car red = new Car(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final Car c1 = new Car(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c1);
        p.getChildren().add(c1);

        final Car c2 = new Car(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c2);
        p.getChildren().add(c2);

        final Car c3 = new Car(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c3);
        p.getChildren().add(c3);

        final Car c4 = new Car(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c4);
        p.getChildren().add(c4);

        final Car c5 = new Car(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c5);
        p.getChildren().add(c5);

        final Car c6 = new Car(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c6);
        p.getChildren().add(c6);

        final Car c7 = new Car(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c7);
        p.getChildren().add(c7);

        final Car c8 = new Car(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext);
        garage.add(c8);
        p.getChildren().add(c8);

        final Car c9 = new Car(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c9);
        p.getChildren().add(c9);

        final Car c10 = new Car(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext);
        garage.add(c10);
        p.getChildren().add(c10);

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
        final int numberLevels = 33;
        level = (level + 1) % numberLevels;
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    private void toWinListener() {
        toWin.xProperty().addListener((o) -> {
            if (!endOfGame && Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                endOfGame = true;
                gameContext.playWinTransition(500, actionEvent -> {

                    log.debug("you won !");
                    dispose();
                    launch();
                });
            }
        });

        toWin.yProperty().addListener((o) -> {
            if (!endOfGame && Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                endOfGame = true;
                gameContext.playWinTransition(500, actionEvent -> {

                });
            }
        });
    }

    private void setIntersections() {
        for (final Car car : garage) {
            if (car.isDirection()) {
                car.xProperty().addListener((o) -> checkIntersections(car));
            } else {
                car.yProperty().addListener((o) -> checkIntersections(car));
            }
        }
    }

    private void checkIntersections(final Car car) {
        for (final Car car2 : garage) {
            if (car2 != car) {
                if (Shape.intersect(car, car2).getBoundsInLocal().getWidth() != -1) {
                    log.debug("intersect");
                    car.setIntersect(true);
                    car.setSelected(false);
                }
            }
        }
        if (Shape.intersect(car, door).getBoundsInLocal().getWidth() != -1) {
            // do nothingnothing
        } else if ((Shape.intersect(car, up).getBoundsInLocal().getWidth() != -1)
            || (Shape.intersect(car, down).getBoundsInLocal().getWidth() != -1)
            || (Shape.intersect(car, left).getBoundsInLocal().getWidth() != -1)
            || (Shape.intersect(car, right).getBoundsInLocal().getWidth() != -1)) {
            log.debug("intersect");
            car.setIntersect(true);
            car.setSelected(false);
        }

    }

    private void createGarage(final Pane p) {
        final int longueur = garageWidth;
        final int hauteur = garageHeight;

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
