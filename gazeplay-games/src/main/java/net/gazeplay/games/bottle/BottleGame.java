package net.gazeplay.games.bottle;

import javafx.animation.*;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.components.ProgressButton;

import java.util.ArrayList;

public class BottleGame implements GameLifeCycle {

    private final BottleGameStats bottleGameStats;
    private final Dimension2D dimension2D;
    private final Configuration configuration;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Group foregroundLayer;
    private final IGameContext gameContext;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private ArrayList<ProgressButton> bottle;

    private Circle ball;
    private Rectangle bar;
    private Rectangle bar2;
    private final Text scoreText;
    private int score;
    private int nbBottle;

    private boolean isBroken;

    public BottleGame(IGameContext gameContext, BottleGameStats stats, int number) {

        this.bottleGameStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();

        this.bottle = new ArrayList<>();
        this.nbBottle = number;

        this.isBroken = false;
        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        this.foregroundLayer = new Group();
        gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        final Rectangle backgroundImage = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        backgroundImage.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage.heightProperty().bind(gameContext.getRoot().heightProperty());
        backgroundImage.setFill(new ImagePattern(new Image("data/bottle/supermarket.jpg")));

        backgroundLayer.getChildren().add(backgroundImage);

        final int fixationLength = configuration.getFixationLength();

        scoreText = new Text(0, 50, "0");
        scoreText.setFill(Color.WHITE);
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFont(new Font(50));
        scoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().add(scoreText);

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

    }

    @Override
    public void launch() {
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        this.middleLayer.getChildren().clear();
        gameContext.getChildren().clear();
        score = -1;
        updateScore();
        bottle.clear();

        gameContext.getChildren().addAll(backgroundLayer, middleLayer, foregroundLayer);

        initBall();

        createBottle(nbBottle);

        middleLayer.getChildren().add(ball);
        gameContext.getChildren().add(ball);

        initBar();


        bottleGameStats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(bottleGameStats);
    }

    private void initBall() {
        ball = new Circle(Math.min(dimension2D.getWidth() / 40, dimension2D.getHeight() / 20)/2);
        ball.setFill(new ImagePattern(new Image("data/bottle/ball.png")));
        ball.setVisible(false);
    }

    private void initBar() {
        bar = new Rectangle(dimension2D.getWidth() / 10, dimension2D.getHeight() / 7 + dimension2D.getHeight() / 6, dimension2D.getWidth() * 8 / 10, dimension2D.getHeight() / 20);
        bar.setFill(new ImagePattern(new Image("data/bottle/etagere.png")));
        bar2 = new Rectangle(dimension2D.getWidth() / 10, dimension2D.getHeight() / 7 + dimension2D.getHeight() / 3.5 + dimension2D.getHeight() / 6, dimension2D.getWidth() * 8 / 10, dimension2D.getHeight() / 20);
        bar2.setFill(new ImagePattern(new Image("data/bottle/etagere.png")));
        backgroundLayer.getChildren().addAll(bar, bar2);
        gameContext.getChildren().addAll(bar, bar2);
    }

    private void createBottle(final int nb) {
        ProgressButton b;
        double x;
        double y;
        for (int i = 1; i <= nb; i++) {
            if (i < 9) {
                x = dimension2D.getWidth() * i / 10;
                y = dimension2D.getHeight() / 7;
            } else {
                x = dimension2D.getWidth() * (i % 9 + 1) / 10;
                y = dimension2D.getHeight() / 7 + dimension2D.getHeight() / 3.5;
            }
            b = new ProgressButton();
            b.setLayoutX(x);
            b.setLayoutY(y);
            b.getButton().setRadius(35);
            b.getButton().setVisible(false);
            bottle.add(b);
        }

        for (final ProgressButton bo : bottle) {
            ImageView bottleI = new ImageView(new Image("data/bottle/bottle.png"));
            bottleI.setFitWidth(dimension2D.getWidth() / 12);
            bottleI.setFitHeight(dimension2D.getHeight() / 6);
            bo.setImage(bottleI);

            middleLayer.getChildren().add(bo);
            gameContext.getChildren().add(bo);


            bo.assignIndicator(event -> {
                if (!isBroken) {
                    isBroken = true;
                    ballMovement(bo);
                }
            }, configuration.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(bo);
            bo.active();

        }
    }


    private void ballMovement(ProgressButton bottle) {
        ball.toFront();
        ball.setTranslateX(dimension2D.getWidth()/2 );
        ball.setTranslateY(dimension2D.getHeight());
        ball.setVisible(true);

        bottle.setDisable(true);
        bottle.getButton().setDisable(true);

        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0), new KeyValue(ball.rotateProperty(),0)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5), new KeyValue(ball.rotateProperty(),360)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5), new KeyValue(ball.translateYProperty(), bottle.getLayoutY() + bottle.getHeight() / 2 , Interpolator.EASE_OUT)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5), new KeyValue(ball.translateXProperty(), bottle.getLayoutX() + bottle.getWidth() / 2 , Interpolator.LINEAR)));
        timeline.setOnFinished(event -> {
            bottleBreaker(bottle);
            ball.setVisible(false);
            gameContext.getSoundManager().add("data/bottle/sounds/verre.wav");
            bottleGameStats.incrementNumberOfGoalsReached();
            updateScore();
        });
        timeline.play();
    }

    private void bottleBreaker(ProgressButton bottle) {
        ImageView bottleI = new ImageView(new Image("data/bottle/broken.png"));
        bottleI.setFitWidth(dimension2D.getWidth() / 12);
        bottleI.setFitHeight(dimension2D.getHeight() / 6);
        bottle.setImage(bottleI);

        FadeTransition bottleDisappear = new FadeTransition(Duration.seconds(1), bottle);
        bottleDisappear.setFromValue(1);
        bottleDisappear.setToValue(0);
        bottleDisappear.setCycleCount(1);
        bottleDisappear.setInterpolator(Interpolator.LINEAR);
        bottleDisappear.play();
        bottleDisappear.setOnFinished(event -> bottle.disable());
        isBroken = false;
    }

    private void updateScore() {
        score = score + 1;
        scoreText.setText(String.valueOf(score));
        scoreText.setX(dimension2D.getWidth() / 2 - scoreText.getWrappingWidth() / 2);
        if (score == nbBottle) {
            gameContext.playWinTransition(0, event1 -> {
                gameContext.clear();
                gameContext.showRoundStats(bottleGameStats, this);
            });
        }
    }

    @Override
    public void dispose() {

    }


}
