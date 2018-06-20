package net.gazeplay.games.pet;

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
    public int[] it = { 20, 20, 20 };

    private final GameContext gameContext;
    private final Stats stats;

    @Getter
    @Setter
    private int mode;

    @Getter
    private Rectangle background;

    public PetHouse(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;

        setMode(INIT_MODE);

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        this.background.setFill(Color.BEIGE /* new ImagePattern(new Image("background.jpg")) */);
        gameContext.getChildren().add(this.background);

        Rectangle zone = new Rectangle(0, 0, dimension2D.getWidth() / 1.7, (2 * dimension2D.getHeight()) / 2.5);
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

        double offset = dimension2D.getHeight() / 20;

        Bars.setSpacing(offset);

        Bars.getChildren().addAll(createColoredProgressBar(Color.BLUE), createColoredProgressBar(Color.RED),
                createColoredProgressBar(Color.GREEN));

        return Bars;
    }

    public HBox createColoredProgressBar(Color c) {
        HBox Bar = new HBox();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double size = dimension2D.getHeight() / 20;

        for (int i = 0; i < 20; i++) {
            Rectangle r = new Rectangle(0, 0, (6 * size) / 20, size);
            r.setFill(c);
            Bar.getChildren().add(r);
        }
        int index = getIt(c);

        Timeline t = new Timeline();
        t.setDelay(Duration.millis(500));
        t.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));
        int i = 5;

        t.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                int index = getIt(c);
                if (index != -1) {
                    t.getKeyFrames().clear();
                    t.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                            new KeyValue(((Rectangle) Bar.getChildren().get(index)).fillProperty(), Color.WHITE)));
                    t.play();
                } else {
                    t.stop();
                }
            }
        });

        t.play();

        return Bar;
    }

    public int getIt(Color c) {
        int i = 0;
        if (c == Color.BLUE) {
            i = 0;
        } else if (c == Color.RED) {
            i = 1;
        } else {
            i = 2;
        }
        int temp = it[i];
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
            EventHandler<Event> buttonHandler = createprogessButtonHandler();
            ImageView iv = new ImageView(new Image("data/cake/menu" + i + ".png"));
            iv.setFitWidth(2 * buttonSize / 3);
            iv.setPreserveRatio(true);
            bt.setImage(iv);
            bt.button.setRadius(buttonSize / 2);
            bt.setLayoutY((((i % 2) + 1) * (dimension2D.getHeight() / 2.5)) - (buttonSize * 1.5));

            if (i < 2) {
                bt.setLayoutX(dimension2D.getWidth() - buttonSize * 1.2);
                buttonHandler = new EventHandler<Event>() {
                    @Override
                    public void handle(Event e) {
                        background.setFill(Color.BEIGE /* new ImagePattern(new Image("background.jpg")) */);
                    }
                };
            } else {
                bt.setLayoutX(buttonSize * 0.2);
            }

            bt.assignIndicator(buttonHandler, Configuration.getInstance().getFixationLength());
            bt.active();
            this.getChildren().add(bt);
            gameContext.getGazeDeviceManager().addEventFilter(bt.button);
        }
    }

    public EventHandler<Event> createprogessButtonHandler() {
        EventHandler<Event> buttonHandler;
        buttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                background.setFill(Color.CORAL /* new ImagePattern(new Image("background.jpg")) */);
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
