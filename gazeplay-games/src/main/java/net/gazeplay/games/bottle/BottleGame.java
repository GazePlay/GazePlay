package net.gazeplay.games.bottle;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.components.ProgressButton;

import java.util.Random;

@Slf4j
public class BottleGame extends AnimationTimer implements GameLifeCycle {

    private final BottleGameStats bottleGameStats;
    private final Dimension2D dimension2D;
    private final Random random;
    private final Configuration configuration;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Rectangle interactionOverlay;
    private final IGameContext gameContext;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private ProgressButton bottle1;
    private ProgressButton bottle2;
    private Rectangle ball;

    private Point2D gazeTarget;

    private final Multilinguism translate;

    public BottleGame(IGameContext gameContext, BottleGameStats stats) {

        this.bottleGameStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.random = new Random();
        this.configuration = gameContext.getConfiguration();

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        final StackPane sp = new StackPane();
        gameContext.getChildren().addAll(sp, backgroundLayer, middleLayer, foregroundLayer);

        this.translate = MultilinguismFactory.getSingleton();

        final Rectangle backgroundImage = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        backgroundImage.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage.setFill(new ImagePattern(new Image("data/bottle/supermarket.png")));

        sp.getChildren().add(backgroundImage);

        // Menu
        final int fixationLength = configuration.getFixationLength();

        shade = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        shade.setFill(new Color(0, 0, 0, 0.75));

        restartButton = new ProgressButton();
        final String dataPath = "data/space";
        final ImageView restartImage = new ImageView(dataPath + "/menu/restart.png");
        restartImage.setFitHeight(dimension2D.getHeight() / 6);
        restartImage.setFitWidth(dimension2D.getHeight() / 6);
        restartButton.setImage(restartImage);
        restartButton.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getHeight() / 12);
        restartButton.setLayoutY(dimension2D.getHeight() / 2 - dimension2D.getHeight() / 12);
        restartButton.assignIndicator(event -> launch(), fixationLength);

        finalScoreText = new Text(0, dimension2D.getHeight() / 4, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        gameContext.getGazeDeviceManager().addEventFilter(restartButton);

        // Interaction
        gazeTarget = new Point2D(dimension2D.getWidth() / 2, dimension2D.getHeight() / 2);

        interactionOverlay = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());

        final EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                gazeTarget = new Point2D(((GazeEvent) event).getX(), ((GazeEvent) event).getY());
            }
        };

        interactionOverlay.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        interactionOverlay.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
        interactionOverlay.setFill(Color.TRANSPARENT);
        foregroundLayer.getChildren().add(interactionOverlay);

        gameContext.getGazeDeviceManager().addEventFilter(interactionOverlay);
    }

    @Override
    public void launch() {
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();

        gazeTarget = new Point2D(dimension2D.getWidth() / 2, 0);

        initBall();

        bottle1 = new ProgressButton();
        bottle1.setLayoutX(dimension2D.getWidth() / 10);
        bottle1.setLayoutY(dimension2D.getHeight() / 3);
        bottle1.getButton().setRadius(35);
        ImageView bottleI = new ImageView(new Image("data/bottle/bottle.png"));
        bottleI.setFitWidth(dimension2D.getWidth() / 10);
        bottleI.setFitHeight(dimension2D.getHeight() / 5);
        bottle1.setImage(bottleI);

        bottle1.assignIndicator(event -> {
            bottleGameStats.incrementNumberOfGoalsReached();
            ball.setVisible(true);
            ballMovement(bottle1);
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(bottle1);
        bottle1.active();

        bottle2 = new ProgressButton();
        bottle2.setLayoutX(dimension2D.getWidth() / 5);
        bottle2.setLayoutY(dimension2D.getHeight() / 3);
        bottle2.getButton().setRadius(35);
        ImageView bottle2I = new ImageView(new Image("data/bottle/bottle.png"));
        bottle2I.setFitWidth(dimension2D.getWidth() / 10);
        bottle2I.setFitHeight(dimension2D.getHeight() / 5);
        bottle2.setImage(bottle2I);

        bottle2.assignIndicator(event -> {
            bottleGameStats.incrementNumberOfGoalsReached();
            ball.setVisible(true);
            ballMovement(bottle2);
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(bottle2);
        bottle2.active();

        middleLayer.getChildren().addAll(bottle1, bottle2, ball);

        gameContext.getChildren().addAll(bottle1, bottle2, ball);

        bottleGameStats.notifyNewRoundReady();
    }

    @Override
    public void handle(long l) {

    }

    private void initBall() {
        ball = new Rectangle(dimension2D.getWidth() * 39 / 40, dimension2D.getHeight() * 19 / 20, dimension2D.getWidth() / 40, dimension2D.getHeight() / 20);
        ball.setFill(new ImagePattern(new Image("data/bottle/ball.png")));
        ball.setVisible(false);
    }

    private void ballMovement(ProgressButton bottle) {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2), new KeyValue(ball.translateYProperty(), bottle.getLayoutY() + bottle.getHeight() / 2 - ball.getY(), Interpolator.LINEAR)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2), new KeyValue(ball.translateXProperty(), bottle.getLayoutX() + bottle.getWidth() / 2 - ball.getX(), Interpolator.LINEAR)));
        timeline.setOnFinished(event -> {
            ball.setVisible(false);
            bottleBreaker(bottle);
        });
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0), new KeyValue(ball.translateYProperty(), bottle.getLayoutY() - bottle.getHeight() / 2 + ball.getY(), Interpolator.LINEAR)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0), new KeyValue(ball.translateXProperty(), bottle.getLayoutX() - bottle.getWidth() / 2 + ball.getX(), Interpolator.LINEAR)));
        timeline.play();
    }

    private void bottleBreaker(ProgressButton bottle) {
        ImageView bottleI = new ImageView(new Image("data/bottle/tequila.png"));
        bottleI.setFitWidth(dimension2D.getWidth() / 10);
        bottleI.setFitHeight(dimension2D.getHeight() / 5);
        bottle.setImage(bottleI);

        FadeTransition bottleDisappear = new FadeTransition(Duration.seconds(1), bottle);
        bottleDisappear.setFromValue(1);
        bottleDisappear.setToValue(0);
        bottleDisappear.setCycleCount(1);
        bottleDisappear.setInterpolator(Interpolator.LINEAR);
        bottleDisappear.play();
        bottleDisappear.setOnFinished(event -> {
            bottle.disable();
        });
    }

    @Override
    public void dispose() {

    }


}


