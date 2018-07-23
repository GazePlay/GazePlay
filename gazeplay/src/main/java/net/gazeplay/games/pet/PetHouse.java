package net.gazeplay.games.pet;

import com.sun.glass.ui.Cursor;
import com.sun.glass.ui.Screen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
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
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.stats.Stats;
import tobii.Tobii;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PetHouse extends Parent implements GameLifeCycle {

    public final static int INIT_MODE = 1;
    public final static int BATH_MODE = 3;
    public final static int EAT_MODE = 2;
    public final static int SPORT_MODE = 0;

    @Getter
    private final GameContext gameContext;

    @Getter
    private final Stats stats;
    private final static int LIFE_SIZE = 18;
    private List<Circle> water;

    private final double handSize;

    private Mypet pet;
    private HBox Bars;
    private ImageView bowl;

    private Integer waterNeeded = 100;

    public int[] it = { LIFE_SIZE, LIFE_SIZE, LIFE_SIZE };
    public Timeline[] timelines = { new Timeline(), new Timeline(), new Timeline() };
    private final Color[] color = { Color.DARKSEAGREEN, Color.ALICEBLUE, Color.DARKSALMON, Color.LAVENDER };
    private final String[] screen = { "park.jpg", "room.jpg", "kitchen.jpg", "shower.jpg" };
    private final String[] cursor = { "glove.png", "hand.png", "emptyspoon.png", "pommeau.png" };
    private final Color[] colorBar = { Color.BLUE, Color.RED, Color.GREEN };
    private final double[] regressionTime = { 1, 2, 4 };

    @Getter
    @Setter
    private Boolean baloonGone = false;

    public Timeline rd;

    @Getter
    @Setter
    private int mode;

    private boolean inside = false;

    @Getter
    @Setter
    private boolean spoonFull = false;

    @Getter
    private Rectangle background;

    @Getter
    private Rectangle zone;

    private boolean enterZone = false;

    public Rectangle hand;

    private final int screenWidth;
    private final int screenHeight;

    public PetHouse(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;

        Screen mainScreen = Screen.getMainScreen();
        screenWidth = mainScreen.getWidth();
        screenHeight = mainScreen.getHeight();

        setMode(INIT_MODE);

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        this.background.setFill(Color.BEIGE /* new ImagePattern(new Image("background.jpg")) */);
        gameContext.getChildren().add(this.background);
        water = new ArrayList();
        rd = new Timeline();

        double facteur = (2 / 2.5) + (1 - 2 / 2.5) / 3;

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

        EventHandler<Event> handevent = new EventHandler<Event>() {

            @Override
            public void handle(Event e) {
                Cursor.setVisible(true);
                inside = true;
                double offsetx = 0;
                double offsety = 0;
                if (mode == EAT_MODE) {
                    offsetx = hand.getWidth() / 4;
                    offsety = -hand.getHeight() / 4;
                }

                if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                    hand.setX(offsetx + MouseInfo.getPointerInfo().getLocation().getX()
                            - gameContext.getGazePlay().getPrimaryStage().getX() - hand.getWidth() / 2);
                    hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                            - gameContext.getGazePlay().getPrimaryStage().getY() - hand.getHeight() / 2);
                } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
                    float[] pointAsFloatArray = Tobii.gazePosition();

                    final float xRatio = pointAsFloatArray[0];
                    final float yRatio = pointAsFloatArray[1];

                    final double positionX = xRatio * screenWidth;
                    final double positionY = yRatio * screenHeight;

                    hand.setX(offsetx + positionX - gameContext.getGazePlay().getPrimaryStage().getX()
                            - hand.getWidth() / 2);
                    hand.setY(offsety + positionY - gameContext.getGazePlay().getPrimaryStage().getY()
                            - hand.getHeight() / 2);
                }

                hand.toFront();
                hand.setVisible(true);

            }

        };

        pet.addEventFilter(MouseEvent.MOUSE_MOVED, handevent);
        pet.addEventFilter(GazeEvent.GAZE_MOVED, handevent);

        gameContext.getChildren().add(pet);
        activateScreen(INIT_MODE);

        createButtons();

        stats.notifyNewRoundReady();

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void createZoneEvents() {
        EventHandler<Event> handevent = new EventHandler<Event>() {

            @Override
            public void handle(Event e) {
                Cursor.setVisible(true);
                double offsetx = 0;
                double offsety = 0;
                if (mode == EAT_MODE) {
                    offsetx = hand.getWidth() / 4;
                    offsety = -hand.getHeight() / 4;
                }
                if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                    hand.setX(offsetx + MouseInfo.getPointerInfo().getLocation().getX()
                            - gameContext.getGazePlay().getPrimaryStage().getX() - hand.getWidth() / 2);
                    hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                            - gameContext.getGazePlay().getPrimaryStage().getY() - hand.getHeight() / 2);
                } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
                    float[] pointAsFloatArray = Tobii.gazePosition();

                    final float xRatio = pointAsFloatArray[0];
                    final float yRatio = pointAsFloatArray[1];

                    final double positionX = xRatio * screenWidth;
                    final double positionY = yRatio * screenHeight;

                    hand.setX(offsetx + positionX - gameContext.getGazePlay().getPrimaryStage().getX()
                            - hand.getWidth() / 2);
                    hand.setY(offsety + positionY - gameContext.getGazePlay().getPrimaryStage().getY()
                            - hand.getHeight() / 2);
                }
            }

        };

        zone.addEventFilter(MouseEvent.MOUSE_MOVED, handevent);
        zone.addEventFilter(GazeEvent.GAZE_MOVED, handevent);

        EventHandler<Event> enterevent = new EventHandler<Event>() {

            @Override
            public void handle(Event e) {
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
                            - gameContext.getGazePlay().getPrimaryStage().getX() - hand.getWidth() / 2);
                    hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                            - gameContext.getGazePlay().getPrimaryStage().getY() - hand.getHeight() / 2);

                } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    float[] pointAsFloatArray = Tobii.gazePosition();

                    final float xRatio = pointAsFloatArray[0];
                    final float yRatio = pointAsFloatArray[1];

                    final double positionX = xRatio * screenWidth;
                    final double positionY = yRatio * screenHeight;

                    hand.setX(offsetx + positionX - gameContext.getGazePlay().getPrimaryStage().getX()
                            - hand.getWidth() / 2);
                    hand.setY(offsety + positionY - gameContext.getGazePlay().getPrimaryStage().getY()
                            - hand.getHeight() / 2);
                }
            }

        };

        zone.addEventFilter(MouseEvent.MOUSE_ENTERED, enterevent);
        zone.addEventFilter(GazeEvent.GAZE_ENTERED, enterevent);

        EventHandler<Event> outhandevent = new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                inside = false;
                Cursor.setVisible(true);
                hand.toBack();
                hand.setVisible(false);
            }

        };

        zone.addEventFilter(MouseEvent.MOUSE_EXITED, outhandevent);
        zone.addEventFilter(GazeEvent.GAZE_EXITED, outhandevent);
    }

    public HBox createBars() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        HBox Bars = new HBox();

        Bars.widthProperty().addListener((observable, oldValue, newValue) -> {
            Bars.setLayoutX(dimension2D.getWidth() / 2 - newValue.doubleValue() / 2);
        });

        Bars.heightProperty().addListener((observable, oldValue, newValue) -> {
            Bars.setLayoutY(newValue.doubleValue() / 2);
        });

        double offset = dimension2D.getHeight() / LIFE_SIZE;

        Bars.setSpacing(offset);

        Bars.getChildren().addAll(createColoredProgressBar(0), createColoredProgressBar(1),
                createColoredProgressBar(2));

        return Bars;
    }

    public HBox createColoredProgressBar(int numero) {
        HBox Bar = new HBox();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double size = dimension2D.getHeight() / 20;

        for (int i = 0; i < LIFE_SIZE; i++) {
            Rectangle r = new Rectangle(0, 0, (6 * size) / LIFE_SIZE, size);
            r.setFill(colorBar[numero]);
            Bar.getChildren().add(r);
        }

        return Bar;
    }

    public void activateBars() {
        for (int i = 0; i < 3; i++) {
            int index = getIt(i);
            HBox Bar = (HBox) Bars.getChildren().get(i);

            timelines[i] = new Timeline();
            timelines[i].setDelay(Duration.seconds(Configuration.getInstance().getSpeedEffects() * regressionTime[i]));
            timelines[i].getKeyFrames().add(new KeyFrame(Duration.millis(500),
                    new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));

            final int number = i;
            timelines[i].setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    int index = getIt(number);
                    if (index >= 0) {
                        timelines[number].getKeyFrames().clear();
                        timelines[number].setDelay(Duration
                                .seconds(Configuration.getInstance().getSpeedEffects() * regressionTime[number]));
                        timelines[number].getKeyFrames().add(new KeyFrame(Duration.millis(500),
                                new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));
                        timelines[number].play();
                    } else {
                        timelines[number].stop();
                    }
                }
            });

            timelines[i].play();
        }
    }

    public void createHand() {

        hand = new Rectangle(0, 0, handSize, handSize);
        hand.setMouseTransparent(true);
        hand.toBack();
        hand.setVisible(false);

        gameContext.getChildren().add(hand);
    }

    public int getIt(int i) {
        it[i]--;
        return it[i];
    }

    public void createButtons() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double buttonSize = dimension2D.getHeight() / 4;
        for (int i = 0; i < 4; i++) {
            ProgressButton bt = new ProgressButton();
            bt.button.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                    + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                    + "-fx-max-height: " + buttonSize + "px;");
            ImageView iv = new ImageView(new Image("data/pet/images/menu" + i + ".png"));

            iv.setFitWidth(2 * buttonSize / 3);
            iv.setPreserveRatio(true);
            bt.setImage(iv);
            bt.button.setRadius(buttonSize / 2);
            bt.setLayoutY((((i % 2) + 1) * (dimension2D.getHeight() / 2.5)) - (buttonSize * 1.5));

            EventHandler<Event> buttonHandler = createprogessButtonHandler(i);

            if (i < 2) {
                bt.setLayoutX(dimension2D.getWidth() - buttonSize * 1.2);
            } else {
                bt.setLayoutX(buttonSize * 0.2);
            }

            bt.assignIndicator(buttonHandler, Configuration.getInstance().getFixationLength());
            bt.active();
            this.getChildren().add(bt);
            gameContext.getGazeDeviceManager().addEventFilter(bt.button);
            bt.toFront();
        }
    }

    public EventHandler<Event> createprogessButtonHandler(int number) {
        EventHandler<Event> buttonHandler;
        buttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                activateScreen(number);
            }
        };
        return buttonHandler;
    }

    public void activateScreen(int number) {
        turnOffShower();
        stopSport();
        gameContext.getChildren().remove(bowl);
        pet.setBasic();
        setMode(number);

        background.setFill(color[number % 4]);
        int j = 1;
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

        Timeline t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(pet.scaleXProperty(), j)));
        t.getKeyFrames().add(new KeyFrame(Duration.millis(200), new KeyValue(pet.scaleYProperty(), j)));
        t.play();

        zone.setFill(new ImagePattern(new Image("data/pet/images/" + screen[number % 4]), 0, 0, 1, 1, true));

    }

    public void refill(int i) {
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

    public void turnOffShower() {
        for (Circle c : water) {
            gameContext.getChildren().remove(c);
        }
        water.clear();

    }

    public void turnOnShower() {
        water = new ArrayList<Circle>();
        for (int i = 0; i < 50; i++) {
            Circle c = new Circle();
            water.add(c);
            c.toFront();
            c.setMouseTransparent(true);
            c.setOpacity(0);
            c.setRadius(hand.getHeight() / 20);
            c.setFill(Color.AQUA);

            gameContext.getChildren().add(c);
            TranslateTransition t = new TranslateTransition(Duration.seconds(1 + Math.random() * 1), c);
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

                        synchronized (waterNeeded) {
                            waterNeeded--;
                            if (waterNeeded <= 0) {
                                refill(0);
                                waterNeeded = 100;
                            }
                        }
                        ;
                    } else {
                        c.setOpacity(0);
                        t.play();
                    }

                }

            });

            t.setFromX(hand.getX() + Math.random() * (hand.getWidth() / 2));
            t.setFromY(hand.getY() + hand.getHeight() / 2);
            t.setToY(zone.getHeight());
            t.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
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

                }
            });
            t.play();
        }

    }

    public void doSport() {
        double coefx = (zone.getWidth() - zone.getWidth() / 3);
        double coefy = (zone.getHeight() - zone.getHeight() / 3);
        double xpos0 = zone.getX() + Math.random() * coefx;
        double ypos0 = zone.getY() + Math.random() * coefy;
        rd = new Timeline();
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(Configuration.getInstance().getSpeedEffects() * 1000),
                new KeyValue(pet.layoutXProperty(), xpos0)));
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(Configuration.getInstance().getSpeedEffects() * 1000),
                new KeyValue(pet.layoutYProperty(), ypos0)));
        rd.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                rd.getKeyFrames().clear();
                double xpos = zone.getX() + Math.random() * coefx;
                double ypos = zone.getY() + Math.random() * coefy;
                rd.getKeyFrames()
                        .add(new KeyFrame(Duration.millis(Configuration.getInstance().getSpeedEffects() * 1000),
                                new KeyValue(pet.layoutXProperty(), xpos)));
                rd.getKeyFrames()
                        .add(new KeyFrame(Duration.millis(Configuration.getInstance().getSpeedEffects() * 1000),
                                new KeyValue(pet.layoutYProperty(), ypos)));
                rd.play();
            }
        });
        rd.play();
    }

    public void stopSport() {
        rd.stop();
        rd.getKeyFrames().clear();
        double xpos = zone.getX() + zone.getWidth() / 2 - pet.getBiboulew() / 2;
        double ypos = zone.getY() + zone.getHeight() / 2 - pet.getBibouleh() / 2;
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(pet.layoutXProperty(), xpos)));
        rd.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(pet.layoutYProperty(), ypos)));
        rd.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                // do nothing
            }
        });

        rd.play();
    }

    public void letsEat() {

        bowl = new ImageView(new Image("data/pet/images/bowl.png"));
        bowl.setPreserveRatio(true);
        if (zone.getWidth() > zone.getHeight()) {
            bowl.setFitHeight(zone.getHeight() / 5);
        } else {
            bowl.setFitWidth(zone.getWidth() / 4);
        }

        bowl.setY(zone.getY() + 4 * zone.getHeight() / 5);
        bowl.setX(zone.getX());

        EventHandler<Event> handevent = new EventHandler<Event>() {

            @Override
            public void handle(Event e) {
                double offsetx = 0;
                double offsety = 0;
                if (mode == EAT_MODE) {
                    offsetx = hand.getWidth() / 4;
                    offsety = -hand.getHeight() / 4;
                }
                if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                    hand.setX(offsetx + MouseInfo.getPointerInfo().getLocation().getX()
                            - gameContext.getGazePlay().getPrimaryStage().getX() - hand.getWidth() / 2);
                    hand.setY(offsety + MouseInfo.getPointerInfo().getLocation().getY()
                            - gameContext.getGazePlay().getPrimaryStage().getY() - hand.getHeight() / 2);

                } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
                    float[] pointAsFloatArray = Tobii.gazePosition();

                    final float xRatio = pointAsFloatArray[0];
                    final float yRatio = pointAsFloatArray[1];

                    final double positionX = xRatio * screenWidth;
                    final double positionY = yRatio * screenHeight;

                    hand.setX(offsetx + positionX - gameContext.getGazePlay().getPrimaryStage().getX()
                            - hand.getWidth() / 2);
                    hand.setY(offsety + positionY - gameContext.getGazePlay().getPrimaryStage().getY()
                            - hand.getHeight() / 2);
                }

                hand.toFront();
                hand.setVisible(true);

            }

        };

        EventHandler<Event> handenter = new EventHandler<Event>() {

            @Override
            public void handle(Event e) {
                pet.setBasic();
                pet.setBlinkingEnabled(true);
                Cursor.setVisible(true);
                inside = true;
                hand.setFill(new ImagePattern(new Image("data/pet/images/fullspoon.png")));
                setSpoonFull(true);
            }
        };

        bowl.addEventFilter(MouseEvent.MOUSE_ENTERED, handenter);
        bowl.addEventFilter(GazeEvent.GAZE_ENTERED, handenter);

        bowl.addEventFilter(MouseEvent.MOUSE_MOVED, handevent);
        bowl.addEventFilter(GazeEvent.GAZE_MOVED, handevent);
        gameContext.getGazeDeviceManager().addEventFilter(bowl);

        gameContext.getChildren().add(bowl);

    }

    public void setBath() {

    }

    public void setLunch() {

    }

    public void setSports() {

    }

}
