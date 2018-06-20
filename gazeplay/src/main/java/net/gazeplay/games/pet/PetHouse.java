package net.gazeplay.games.pet;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cakes.CakeFactory;

public class PetHouse extends Parent implements GameLifeCycle {

    private final static int INIT_MODE = 0;
    private final static int BATH_MODE = 1;
    private final static int EAT_MODE = 2;
    private final static int SPORT_MODE = 3;

    private final GameContext gameContext;
    private final Stats stats;
    private final static int LIFE_SIZE = 18;

    public int[] it = { LIFE_SIZE, LIFE_SIZE, LIFE_SIZE };
    public Timeline[] t = { new Timeline(), new Timeline(), new Timeline() };
    private final Color[] color = { Color.DARKSEAGREEN, Color.ALICEBLUE, Color.DARKSALMON, Color.LAVENDER };
    private final String[] screen = { "park.jpg", "room.jpg", "kitchen.jpg", "shower.jpg" };
    private final Color[] colorBar = { Color.BLUE, Color.RED, Color.GREEN };

    @Getter
    @Setter
    private int mode;

    @Getter
    private Rectangle background;

    @Getter
    private Rectangle zone;

    public PetHouse(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;

        setMode(INIT_MODE);

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        this.background.setFill(Color.BEIGE /* new ImagePattern(new Image("background.jpg")) */);
        gameContext.getChildren().add(this.background);

        double facteur = (2 / 2.5) + (1 - 2 / 2.5) / 3;

        zone = new Rectangle(0, 0, dimension2D.getWidth() / 1.7, facteur * dimension2D.getHeight());

        zone.setFill(Color.WHITE /* new ImagePattern(new Image("background.jpg")) */);
        zone.setX(dimension2D.getWidth() / 2 - dimension2D.getWidth() / (1.7 * 2));
        zone.setY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 2.5);

        gameContext.getChildren().add(zone);
        zone.toFront();

        gameContext.getChildren().add(this);
    }

    @Override
    public void launch() {

        createButtons();

        HBox Bar = createBars();
        gameContext.getChildren().add(Bar);

        //gameContext.getChildren().add(new Mypet());

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

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

        int i = numero;

        int index = getIt(i);

        t[i] = new Timeline();
        t[i].setDelay(Duration.millis(500));
        t[i].getKeyFrames().add(new KeyFrame(Duration.millis(500),
                new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));

        final int number = i;
        t[i].setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                int index = getIt(number);
                if (index != -1) {
                    t[number].getKeyFrames().clear();
                    t[number].getKeyFrames().add(new KeyFrame(Duration.millis(500),
                            new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));
                    t[number].play();
                } else {
                    t[number].stop();
                }
            }
        });

        t[i].play();

        return Bar;
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
            ImageView iv = new ImageView(new Image("data/cake/menu" + i + ".png"));
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
        }
    }

    public EventHandler<Event> createprogessButtonHandler(int number) {
        EventHandler<Event> buttonHandler;
        buttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                background.setFill(color[number % 4]);
                zone.setFill(new ImagePattern(new Image("data/pet/images/" + screen[number % 4]), 0, 0, 1, 1, true));
                if (t[number % 3].getStatus() == Status.STOPPED) {
                    t[number % 3].play();
                } else {
                    t[number % 3].stop();
                }
            }
        };
        return buttonHandler;
    }

    public void setBath() {

    }

    public void setLunch() {

    }

    public void setSports() {

    }

}
