package net.gazeplay.games.pet;

import com.sun.glass.ui.Cursor;
import com.sun.glass.ui.Screen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;
import tobii.Tobii;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PetHouse extends Parent implements GameLifeCycle {

    final static int INIT_MODE = 1;
    private final static int BATH_MODE = 3;
    final static int EAT_MODE = 2;
    final static int SPORT_MODE = 0;

    @Getter
    private final IGameContext gameContext;

    @Getter
    private final Stats stats;
    private final static int LIFE_SIZE = 18;
    private List<Circle> water;

    private final double handSize;

    private Mypet pet;
    private HBox Bars;
    private ImageView bowl;

    private Integer waterNeeded = 100;

    public final int[] it = {LIFE_SIZE, LIFE_SIZE, LIFE_SIZE};
    private final Timeline[] timelines = {new Timeline(), new Timeline(), new Timeline()};
    private final Color[] color = {Color.DARKSEAGREEN, Color.ALICEBLUE, Color.DARKSALMON, Color.LAVENDER};
    private final String[] screen = {"park.jpg", "room.jpg", "kitchen.jpg", "shower.jpg"};
    private final String[] cursor = {"glove.png", "hand.png", "emptyspoon.png", "pommeau.png"};
    private final Color[] colorBar = {Color.BLUE, Color.RED, Color.GREEN};
    private final double[] regressionTime = {1, 2, 4};

    @Getter
    @Setter
    private Boolean baloonGone = false;

    Timeline rd;

    @Getter
    @Setter
    private int mode;

    private boolean inside = false;

    @Getter
    @Setter
    private boolean spoonFull = false;

    @Getter
    private final Rectangle background;

    @Getter
    private final Rectangle zone;

    @Getter
    private Rectangle hand;

    private final int screenWidth;
    private final int screenHeight;

    PetHouse(final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;

        final Screen mainScreen = Screen.getMainScreen();
        screenWidth = mainScreen.getWidth();
        screenHeight = mainScreen.getHeight();

        setMode(INIT_MODE);

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        this.background.setFill(Color.BEIGE /* new ImagePattern(new Image("background.jpg")) */);
        gameContext.getChildren().add(this.background);
        water = new ArrayList<>();
        rd = new Timeline();

        final double facteur = (2 / 2.5) + (1 - 2 / 2.5) / 3;

        zone = new Rectangle(0, 0, dimension2D.getWidth() / 1.7, facteur * dimension2D.getHeight());

        zone.setFill(Color.WHITE);
        zone.setX(dimension2D.getWidth() / 2 - dimension2D.getWidth() / (1.7 * 2));
        zone.setY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 2.5);

        handSize = (zone.getWidth() > zone.getHeight()) ? zone.getHeight() / 10 : zone.getWidth() / 10;

        createHand();

        createZoneEvents();

        gameContext.getChildren().add(zone);
        zone.toFront();

        gameContext.getGazeDeviceManager().addEventFilter(zone);

        gameContext.getChildren().add(this);
    }

    @Override
    public void launch() {

        Bars = createBars();
        gameContext.getChildren().add(Bars);

        activateBars();

        pet = new Mypet(zone.getHeight(), zone.getWidth(), this);

        pet.setLayoutX(zone.getX() + zone.getWidth() / 2 - pet.getBiboulew() / 2);
        pet.setLayoutY(zone.getY() + zone.getHeight() / 2 - pet.getBibouleh() / 2);

        final EventHandler<Event> handevent = e -> {
            Cursor.setVisible(true);
            inside = true;
            onEvent(e);
        };

        pet.addEventFilter(MouseEvent.MOUSE_MOVED, handevent);
        pet.addEventFilter(GazeEvent.GAZE_MOVED, handevent);

        gameContext.getChildren().add(pet);
        activateScreen(INIT_MODE);

        createButtons();

        stats.notifyNewRoundReady();

    }

    private void onEvent(final Event e) {
        double offsetx = 0;
        double offsety = 0;
        if (mode == EAT_MODE) {
            offsetx = hand.getWidth() / 4;
            offsety = -hand.getHeight() / 4;
        }
        if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
            hand.setX(offsetx + MouseInfo.getPointerInfo().getLocation().getX()
                - gameContext.getPrimaryStage().getX() - hand.getWidth() / 2);
            hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                - gameContext.getPrimaryStage().getY() - hand.getHeight() / 2);
        } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
            final float[] pointAsFloatArray = Tobii.gazePosition();

            final float xRatio = pointAsFloatArray[0];
            final float yRatio = pointAsFloatArray[1];

            final double positionX = xRatio * screenWidth;
            final double positionY = yRatio * screenHeight;

            hand.setX(offsetx + positionX - gameContext.getPrimaryStage().getX()
                - hand.getWidth() / 2);
            hand.setY(offsety + positionY - gameContext.getPrimaryStage().getY()
                - hand.getHeight() / 2);
        }

        hand.toFront();
        hand.setVisible(true);
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    private void createZoneEvents() {
        final EventHandler<Event> handevent = e -> {
            Cursor.setVisible(true);
            double offsetx = 0;
            double offsety = 0;
            if (mode == EAT_MODE) {
                offsetx = hand.getWidth() / 4;
                offsety = -hand.getHeight() / 4;
            }
            if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                hand.setX(offsetx + MouseInfo.getPointerInfo().getLocation().getX()
                    - gameContext.getPrimaryStage().getX() - hand.getWidth() / 2);
                hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                    - gameContext.getPrimaryStage().getY() - hand.getHeight() / 2);
            } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
                final float[] pointAsFloatArray = Tobii.gazePosition();

                final float xRatio = pointAsFloatArray[0];
                final float yRatio = pointAsFloatArray[1];

                final double positionX = xRatio * screenWidth;
                final double positionY = yRatio * screenHeight;

                hand.setX(offsetx + positionX - gameContext.getPrimaryStage().getX()
                    - hand.getWidth() / 2);
                hand.setY(offsety + positionY - gameContext.getPrimaryStage().getY()
                    - hand.getHeight() / 2);
            }
        };

        zone.addEventFilter(MouseEvent.MOUSE_MOVED, handevent);
        zone.addEventFilter(GazeEvent.GAZE_MOVED, handevent);

        final EventHandler<Event> enterevent = e -> {
            Cursor.setVisible(false);
            inside = true;
            hand.toFront();
            hand.setVisible(true);
            double offsetx = 0;
            double offsety = 0;
            if (mode == EAT_MODE) {
                offsetx = hand.getWidth() / 4;
                offsety = -hand.getHeight() / 4;
            }
            if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                hand.setX(offsetx + MouseInfo.getPointerInfo().getLocation().getX()
                    - gameContext.getPrimaryStage().getX() - hand.getWidth() / 2);
                hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                    - gameContext.getPrimaryStage().getY() - hand.getHeight() / 2);

            } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                final float[] pointAsFloatArray = Tobii.gazePosition();

                final float xRatio = pointAsFloatArray[0];
                final float yRatio = pointAsFloatArray[1];

                final double positionX = xRatio * screenWidth;
                final double positionY = yRatio * screenHeight;

                hand.setX(offsetx + positionX - gameContext.getPrimaryStage().getX()
                    - hand.getWidth() / 2);
                hand.setY(offsety + positionY - gameContext.getPrimaryStage().getY()
                    - hand.getHeight() / 2);
            }
        };

        zone.addEventFilter(MouseEvent.MOUSE_ENTERED, enterevent);
        zone.addEventFilter(GazeEvent.GAZE_ENTERED, enterevent);

        final EventHandler<Event> outhandevent = event -> {
            inside = false;
            Cursor.setVisible(true);
            hand.toBack();
            hand.setVisible(false);
        };

        zone.addEventFilter(MouseEvent.MOUSE_EXITED, outhandevent);
        zone.addEventFilter(GazeEvent.GAZE_EXITED, outhandevent);
    }

    private HBox createBars() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final HBox Bars = new HBox();

        Bars.widthProperty().addListener((observable, oldValue, newValue) -> Bars.setLayoutX(dimension2D.getWidth() / 2 - newValue.doubleValue() / 2));

        Bars.heightProperty().addListener((observable, oldValue, newValue) -> Bars.setLayoutY(newValue.doubleValue() / 2));

        final double offset = dimension2D.getHeight() / LIFE_SIZE;

        Bars.setSpacing(offset);

        Bars.getChildren().addAll(createColoredProgressBar(0), createColoredProgressBar(1),
            createColoredProgressBar(2));

        return Bars;
    }

    private HBox createColoredProgressBar(final int numero) {
        final HBox Bar = new HBox();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double size = dimension2D.getHeight() / 20;

        for (int i = 0; i < LIFE_SIZE; i++) {
            final Rectangle r = new Rectangle(0, 0, (6 * size) / LIFE_SIZE, size);
            r.setFill(colorBar[numero]);
            Bar.getChildren().add(r);
        }

        return Bar;
    }

    private void activateBars() {
        for (int i = 0; i < 3; i++) {
            final int index = getIt(i);
            final HBox Bar = (HBox) Bars.getChildren().get(i);

            timelines[i] = new Timeline();
            timelines[i].setDelay(Duration.seconds(regressionTime[i]));
            timelines[i].getKeyFrames().add(new KeyFrame(Duration.millis(500),
                new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));
            timelines[i].rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

            final int number = i;
            timelines[i].setOnFinished(e -> {
                final int index1 = getIt(number);
                if (index1 >= 0) {
                    timelines[number].getKeyFrames().clear();
                    timelines[number].setDelay(Duration
                        .seconds(regressionTime[number]));
                    timelines[number].getKeyFrames().add(new KeyFrame(Duration.millis(500),
                        new KeyValue(((Rectangle) Bar.getChildren().get(index1)).fillProperty(), Color.WHITE)));
                    timelines[number].play();
                } else {
                    timelines[number].stop();
                }
            });

            timelines[i].play();
        }
    }

    private void createHand() {

        hand = new Rectangle(0, 0, handSize, handSize);
        hand.setMouseTransparent(true);
        hand.toBack();
        hand.setVisible(false);

        gameContext.getChildren().add(hand);
    }

    private int getIt(final int i) {
        it[i]--;
        return it[i];
    }

    private void createButtons() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double buttonSize = dimension2D.getHeight() / 4;
        for (int i = 0; i < 4; i++) {
            final ProgressButton bt = new ProgressButton();
            bt.getButton().setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                + "-fx-max-height: " + buttonSize + "px;");
            final ImageView iv = new ImageView(new Image("data/pet/images/menu" + i + ".png"));

            iv.setFitWidth(2 * buttonSize / 3);
            iv.setPreserveRatio(true);
            bt.setImage(iv);
            bt.getButton().setRadius(buttonSize / 2);
            bt.setLayoutY((((i % 2) + 1) * (dimension2D.getHeight() / 2.5)) - (buttonSize * 1.5));

            final EventHandler<Event> buttonHandler = createprogessButtonHandler(i);

            if (i < 2) {
                bt.setLayoutX(dimension2D.getWidth() - buttonSize * 1.2);
            } else {
                bt.setLayoutX(buttonSize * 0.2);
            }

            bt.assignIndicator(buttonHandler, gameContext.getConfiguration().getFixationLength());
            bt.active();
            this.getChildren().add(bt);
            gameContext.getGazeDeviceManager().addEventFilter(bt.getButton());
            bt.toFront();
        }
    }

    private EventHandler<Event> createprogessButtonHandler(final int number) {
        final EventHandler<Event> buttonHandler;
        buttonHandler = e -> activateScreen(number);
        return buttonHandler;
    }

    private void activateScreen(final int number) {
        turnOffShower();
        stopSport();
        gameContext.getChildren().remove(bowl);
        pet.setBasic();
        setMode(number);

        background.setFill(color[number % 4]);
        final int j;
        switch (number) {
            case INIT_MODE:
                j = 2;
                hand.setWidth(2 * handSize);
                hand.setHeight(2 * handSize);
                hand.setFill(new ImagePattern(new Image("data/pet/images/" + cursor[number % 4])));
                break;
            case BATH_MODE:
                j = 1;
                hand.setWidth(3 * handSize);
                hand.setHeight(2 * handSize);
                hand.setFill(new ImagePattern(new Image("data/pet/images/" + cursor[number % 4])));
                turnOnShower();
                break;
            case EAT_MODE:
                j = 1;
                hand.setWidth(3 * handSize);
                hand.setHeight(2 * handSize);
                hand.setFill(new ImagePattern(new Image("data/pet/images/" + cursor[number % 4])));
                letsEat();
                break;
            case SPORT_MODE:
                j = 1;
                hand.setWidth(2 * handSize);
                hand.setHeight(2 * handSize);
                hand.setFill(new ImagePattern(new Image("data/pet/images/" + cursor[number % 4])));
                doSport();
                break;
            default:
                j = 1;
        }

        final Timeline t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(pet.scaleXProperty(), j)));
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(pet.scaleYProperty(), j)));
        t.play();

        zone.setFill(new ImagePattern(new Image("data/pet/images/" + screen[number % 4]), 0, 0, 1, 1, true));

    }

    void refill(final int i) {
        if (it[i] < LIFE_SIZE) {
            if ((it[i] < LIFE_SIZE - 2) && (i == 1)) {
                it[i] = it[i] + 3;
            } else if (it[i] < LIFE_SIZE - 1) {
                it[i] = it[i] + 2;
            } else {
                it[i] = it[i] + 1;
            }
            timelines[i].stop();
            timelines[i].getKeyFrames().clear();
            timelines[i].getKeyFrames()
                .add(new KeyFrame(Duration.millis(500), new KeyValue(
                    ((Rectangle) ((HBox) Bars.getChildren().get(i)).getChildren().get(getIt(i))).fillProperty(),
                    Color.WHITE)));

            ((Rectangle) ((HBox) Bars.getChildren().get(i)).getChildren().get(it[i])).setFill(this.colorBar[i]);
            if (it[i] > 0) {
                ((Rectangle) ((HBox) Bars.getChildren().get(i)).getChildren().get(it[i] - 1)).setFill(this.colorBar[i]);
            }
            if ((i != 0) && (it[i] < LIFE_SIZE - 1 && it[i] >= 0)) {
                ((Rectangle) ((HBox) Bars.getChildren().get(i)).getChildren().get(it[i] - 2)).setFill(this.colorBar[i]);
            }
            timelines[i].play();

            stats.incNbGoals();
            stats.notifyNewRoundReady();

        }
    }

    private void turnOffShower() {
        for (final Circle c : water) {
            gameContext.getChildren().remove(c);
        }
        water.clear();

    }

    private void turnOnShower() {
        water = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final Circle c = new Circle();
            water.add(c);
            c.toFront();
            c.setMouseTransparent(true);
            c.setOpacity(0);
            c.setRadius(hand.getHeight() / 20);
            c.setFill(Color.AQUA);

            gameContext.getChildren().add(c);
            final TranslateTransition t = new TranslateTransition(Duration.seconds(1 + Math.random() * 1), c);
            c.translateYProperty().addListener((observable, oldValue, newValue) -> {
                if (pet.localToParent(pet.getChildren().get(2).getBoundsInParent()).contains(c.getBoundsInParent())) {
                    t.stop();
                    if (inside && mode == BATH_MODE) {
                        t.setFromX(hand.getX() + Math.random() * (hand.getWidth() / 2));
                        t.setFromY(hand.getY() + hand.getHeight() / 2);
                        t.setToY(zone.getHeight());
                        t.play();

                        pet.setBlinkingEnabled(false);
                        if (pet.isEyesAreOpen()) {
                            pet.setHappy();
                        }

                        synchronized (PetHouse.this) {
                            waterNeeded--;
                            if (waterNeeded <= 0) {
                                refill(0);
                                waterNeeded = 100;
                            }
                        }
                    } else {
                        c.setOpacity(0);
                        t.play();
                    }

                }

            });

            t.setFromX(hand.getX() + Math.random() * (hand.getWidth() / 2));
            t.setFromY(hand.getY() + hand.getHeight() / 2);
            t.setToY(zone.getHeight());
            t.setOnFinished(e -> {
                if (inside && mode == BATH_MODE) {
                    t.setFromX(hand.getX() + Math.random() * (hand.getWidth() / 2));
                    t.setFromY(hand.getY() + hand.getHeight() / 2);
                    t.setToY(zone.getHeight());
                    c.setOpacity(1);
                    t.play();
                } else if (mode == BATH_MODE) {
                    c.setOpacity(0);
                    t.play();
                } else {
                    t.stop();
                    c.setOpacity(0);
                }

            });
            t.play();
        }

    }

    private void doSport() {
        final double coefx = (zone.getWidth() - zone.getWidth() / 3);
        final double coefy = (zone.getHeight() - zone.getHeight() / 3);
        final double xpos0 = zone.getX() + Math.random() * coefx;
        final double ypos0 = zone.getY() + Math.random() * coefy;
        rd = new Timeline();
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(1000),
            new KeyValue(pet.layoutXProperty(), xpos0)));
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(1000),
            new KeyValue(pet.layoutYProperty(), ypos0)));
        rd.setOnFinished(e -> {
            rd.getKeyFrames().clear();
            final double xpos = zone.getX() + Math.random() * coefx;
            final double ypos = zone.getY() + Math.random() * coefy;
            rd.getKeyFrames()
                .add(new KeyFrame(Duration.millis(1000),
                    new KeyValue(pet.layoutXProperty(), xpos)));
            rd.getKeyFrames()
                .add(new KeyFrame(Duration.millis(1000),
                    new KeyValue(pet.layoutYProperty(), ypos)));
            rd.play();
        });
        rd.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        rd.play();
    }

    private void stopSport() {
        rd.stop();
        rd.getKeyFrames().clear();
        final double xpos = zone.getX() + zone.getWidth() / 2 - pet.getBiboulew() / 2;
        final double ypos = zone.getY() + zone.getHeight() / 2 - pet.getBibouleh() / 2;
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(pet.layoutXProperty(), xpos)));
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(pet.layoutYProperty(), ypos)));
        rd.setOnFinished(e -> {
            // do nothing
        });

        rd.play();
    }

    private void letsEat() {

        bowl = new ImageView(new Image("data/pet/images/bowl.png"));
        bowl.setPreserveRatio(true);
        if (zone.getWidth() > zone.getHeight()) {
            bowl.setFitHeight(zone.getHeight() / 5);
        } else {
            bowl.setFitWidth(zone.getWidth() / 4);
        }

        bowl.setY(zone.getY() + 4 * zone.getHeight() / 5);
        bowl.setX(zone.getX());

        final EventHandler<Event> handevent = this::onEvent;

        final EventHandler<Event> handenter = e -> {
            pet.setBasic();
            pet.setBlinkingEnabled(true);
            Cursor.setVisible(true);
            inside = true;
            hand.setFill(new ImagePattern(new Image("data/pet/images/fullspoon.png")));
            setSpoonFull(true);
        };

        bowl.addEventFilter(MouseEvent.MOUSE_ENTERED, handenter);
        bowl.addEventFilter(GazeEvent.GAZE_ENTERED, handenter);

        bowl.addEventFilter(MouseEvent.MOUSE_MOVED, handevent);
        bowl.addEventFilter(GazeEvent.GAZE_MOVED, handevent);
        gameContext.getGazeDeviceManager().addEventFilter(bowl);

        gameContext.getChildren().add(bowl);

    }

}
