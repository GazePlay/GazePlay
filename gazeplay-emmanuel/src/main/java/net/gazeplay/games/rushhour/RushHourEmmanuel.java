package net.gazeplay.games.rushhour;

import javafx.animation.Transition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GamesRules;
import net.gazeplay.components.SaveData;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class RushHourEmmanuel extends Parent implements GameLifeCycle {

    public final IGameContext gameContext;
    public final Stats stats;
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
    private CarEmmanuel toWin;
    private List<CarEmmanuel> garage;
    private int level = 0;
    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    private final GamesRules gamesRules;
    private final RushHourEmmanuelGameVariant gameVariant;
    private boolean startGame = false;
    public SaveData saveData;

    public RushHourEmmanuel(final IGameContext gameContext, Stats stats, final RushHourEmmanuelGameVariant gameVariant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.gamesRules = new GamesRules();
        this.gameVariant = gameVariant;
        this.saveData = new SaveData(this.stats, gameVariant.getLabel());
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

        this.gameContext.startTimeLimiterEmmanuel(this.saveData);
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
        ground = new Rectangle(); // to avoid NullPointerException
    }

    private void setLevel(final int i) {

        garage = new LinkedList<>();

        size = new SimpleIntegerProperty();

        final ProgressIndicator pi = new ProgressIndicator(0);
        pi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        pi.setMouseTransparent(true);
        IntegerProperty.readOnlyIntegerProperty(size).addListener((observable, oldValue, newValue) -> {
            pi.setPrefSize(newValue.intValue(), newValue.intValue());
            for (final CarEmmanuel CarEmmanuel : garage) {
                CarEmmanuel.update(newValue.intValue());
            }

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            ground.setHeight(newValue.intValue() * (garageHeight + 2));
            ground.setWidth(newValue.intValue() * (garageWidth + 2));

            p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth()/ 2d);
            p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight()/ 2d);
        });

        p = new Pane();

        p.getChildren().add(pi);

        if (this.gameVariant.getLabel().equals("level1")){
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
            }else {
                this.endGame();
            }
        }else if (this.gameVariant.getLabel().equals("level2")){
            if (i == 0) {
                setLevel11(p, pi);
            } else if (i == 1) {
                setLevel12(p, pi);
            } else if (i == 2) {
                setLevel13(p, pi);
            } else if (i == 3) {
                setLevel14(p, pi);
            } else if (i == 4) {
                setLevel15(p, pi);
            } else if (i == 5) {
                setLevel16(p, pi);
            } else if (i == 6) {
                setLevel17(p, pi);
            } else if (i == 7) {
                setLevel18(p, pi);
            } else if (i == 8) {
                setLevel19(p, pi);
            } else if (i == 9) {
                setLevel20(p, pi);
            } else if (i == 10) {
                setLevel21(p, pi);
            }else {
                this.endGame();
            }
        }else {
            if (i == 0) {
                setLevel22(p, pi);
            } else if (i == 1) {
                setLevel23(p, pi);
            } else if (i == 2) {
                setLevel24(p, pi);
            } else if (i == 3) {
                setLevel25(p, pi);
            } else if (i == 4) {
                setLevel26(p, pi);
            } else if (i == 5) {
                setLevel27(p, pi);
            } else if (i == 6) {
                setLevel28(p, pi);
            } else if (i == 7) {
                setLevel29(p, pi);
            } else if (i == 8) {
                setLevel30(p, pi);
            } else if (i == 9) {
                setLevel31(p, pi);
            } else if (i == 10) {
                setLevel32(p, pi);
            }else {
                this.endGame();
            }
        }

        toWinListener();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        p.setLayoutX(dimension2D.getWidth() / 2 - ground.getWidth()/ 2d);
        p.setLayoutY(dimension2D.getHeight() / 2 - ground.getHeight()/ 2d);

        gameContext.getChildren().add(p);

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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(0, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(1, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(0, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(5, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c7 = new CarEmmanuel(2, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(0, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(1, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(2, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(5, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(1, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(3, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(3, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(1, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(4, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(5, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(4, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(3, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(5, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(1, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(2, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(3, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(4, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(2, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(0, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(2, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(1, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(3, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(4, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(2, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(1, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(2, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(5, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(1, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(4, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(2, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c11);
        p.getChildren().add(c11);

        final CarEmmanuel c12 = new CarEmmanuel(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(2, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(1, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(0, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c11);
        p.getChildren().add(c11);

        final CarEmmanuel c12 = new CarEmmanuel(1, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(2, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(1, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(1, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(4, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c11);
        p.getChildren().add(c11);

        final CarEmmanuel c12 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c12);
        p.getChildren().add(c12);

        final CarEmmanuel c13 = new CarEmmanuel(3, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c13);
        p.getChildren().add(c13);

        final CarEmmanuel c14 = new CarEmmanuel(3, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(4, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(5, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(5, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(0, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(1, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(2, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(2, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(1, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(3, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(2, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(1, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(5, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(0, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(3, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(3, 0, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(2, 1, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(2, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(2, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(0, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(0, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 3, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(1, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(1, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(0, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(2, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(0, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(3, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(5, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(3, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(1, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(3, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(4, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(0, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(0, 2, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(5, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(5, 3, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(0, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(3, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(4, 5, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(2, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(4, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(4, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(5, 1, 1, 3, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(0, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(2, 3, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(2, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

        final CarEmmanuel c11 = new CarEmmanuel(3, 4, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c11);
        p.getChildren().add(c11);

        final CarEmmanuel c12 = new CarEmmanuel(3, 5, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
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

        final CarEmmanuel red = new CarEmmanuel(3, 2, 2, 1, Color.RED, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(red);
        p.getChildren().add(red);

        toWin = red;

        final CarEmmanuel c1 = new CarEmmanuel(0, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c1);
        p.getChildren().add(c1);

        final CarEmmanuel c2 = new CarEmmanuel(1, 0, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c2);
        p.getChildren().add(c2);

        final CarEmmanuel c3 = new CarEmmanuel(1, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c3);
        p.getChildren().add(c3);

        final CarEmmanuel c4 = new CarEmmanuel(2, 1, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c4);
        p.getChildren().add(c4);

        final CarEmmanuel c5 = new CarEmmanuel(3, 0, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c5);
        p.getChildren().add(c5);

        final CarEmmanuel c6 = new CarEmmanuel(4, 1, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c6);
        p.getChildren().add(c6);

        final CarEmmanuel c7 = new CarEmmanuel(5, 2, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c7);
        p.getChildren().add(c7);

        final CarEmmanuel c8 = new CarEmmanuel(5, 4, 1, 2, Color.BROWN, false, size.getValue(), pi, gameContext, stats, this);
        garage.add(c8);
        p.getChildren().add(c8);

        final CarEmmanuel c9 = new CarEmmanuel(0, 3, 3, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c9);
        p.getChildren().add(c9);

        final CarEmmanuel c10 = new CarEmmanuel(0, 4, 2, 1, Color.BROWN, true, size.getValue(), pi, gameContext, stats, this);
        garage.add(c10);
        p.getChildren().add(c10);

    }

    @Override
    public void launch() {
        endOfGame = false;
        if (!this.startGame){
            this.startGame = true;
            String rule = "Sélectionnez une voiture pour pouvoir la déplacer en ligne droite \n Vous avez 2 minutes pour terminer le plus de tableaux possibles";
            final Transition animationRules = this.gamesRules.createQuestionTransition(gameContext, rule);
            animationRules.play();
            animationRules.setOnFinished(event -> {
                this.gamesRules.hideQuestionText();
                setLevel(level);
                this.level++;
                if (toWin.isDirection()) {
                    toWin.setFill(new ImagePattern(new Image("data/rushHour/taxiH.png")));
                } else {
                    toWin.setFill(new ImagePattern(new Image("data/rushHour/taxiV.png")));
                }
                toWin.setEffect(null);
                stats.notifyNewRoundReady();
                gameContext.getGazeDeviceManager().addStats(stats);
                gameContext.firstStart();
            });
        }else {
            this.gamesRules.hideQuestionText();
            setLevel(level);
            this.level++;
            if (toWin.isDirection()) {
                toWin.setFill(new ImagePattern(new Image("data/rushHour/taxiH.png")));
            } else {
                toWin.setFill(new ImagePattern(new Image("data/rushHour/taxiV.png")));
            }
            toWin.setEffect(null);
            stats.notifyNewRoundReady();
            gameContext.getGazeDeviceManager().addStats(stats);
            gameContext.firstStart();
        }
    }

    public void endGame(){
        stats.stop();

        gameContext.updateScore(stats, this);
        gameContext.getGazeDeviceManager().clear();
        gameContext.clear();
        gameContext.showRoundStats(stats, this);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
        this.saveData.addMouseMovements(this.stats.fixationSequence.get(0).size());
        this.saveData.addTrackerMovements(this.stats.fixationSequence.get(1).size());
    }

    private void toWinListener() {
        toWin.xProperty().addListener((o) -> {
            if (!endOfGame && Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
                win();
            }
        });

        toWin.yProperty().addListener((o) -> {
            if (!endOfGame && Shape.intersect(toWin, ground).getBoundsInLocal().getWidth() == -1) {
               win();
            }
        });
    }

    private void win () {
        endOfGame = true;
        stats.incrementNumberOfGoalsReached();

        gameContext.updateScore(stats, this);
        dispose();
        launch();
    }

    private void setIntersections() {
        for (final CarEmmanuel CarEmmanuel : garage) {
            if (CarEmmanuel.isDirection()) {
                CarEmmanuel.xProperty().addListener((o) -> checkIntersections(CarEmmanuel));
            } else {
                CarEmmanuel.yProperty().addListener((o) -> checkIntersections(CarEmmanuel));
            }
        }
    }

    private void checkIntersections(final CarEmmanuel CarEmmanuel) {
        for (final CarEmmanuel CarEmmanuel2 : garage) {
            if (CarEmmanuel2 != CarEmmanuel) {
                if (Shape.intersect(CarEmmanuel, CarEmmanuel2).getBoundsInLocal().getWidth() != -1) {
                    log.debug("intersect");
                    CarEmmanuel.setIntersect(true);
                    CarEmmanuel.setSelected(false);
                }
            }
        }
        if (Shape.intersect(CarEmmanuel, door).getBoundsInLocal().getWidth() != -1) {
            // do nothingnothing
        } else if ((Shape.intersect(CarEmmanuel, up).getBoundsInLocal().getWidth() != -1)
            || (Shape.intersect(CarEmmanuel, down).getBoundsInLocal().getWidth() != -1)
            || (Shape.intersect(CarEmmanuel, left).getBoundsInLocal().getWidth() != -1)
            || (Shape.intersect(CarEmmanuel, right).getBoundsInLocal().getWidth() != -1)) {
            log.debug("intersect");
            CarEmmanuel.setIntersect(true);
            CarEmmanuel.setSelected(false);
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

    public void updateScore(){
        gameContext.updateScore(stats, this);
    }

    private class CustomInputEventHandlerKeyboard implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent key) {

            if (key.getCode().isArrowKey()){
                launch();
            }
        }
    }
}
