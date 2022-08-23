package net.gazeplay.games.rockPaperScissors;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.components.ProgressButton;

@Slf4j
public class RockPaperScissorsGame extends AnimationTimer implements GameLifeCycle {

    private final IGameContext gameContext;
    private final RockPaperScissorsStats stats;
    private final ReplayablePseudoRandom random;

    private final ReadOnlyDoubleProperty widthProperty;
    private final ReadOnlyDoubleProperty heightProperty;

    private final Group layout;

    private int playerScore;
    private int opponentScore;
    private final StringProperty playerScoreTextProperty;
    private final StringProperty opponentScoreTextProperty;

    private HandSign opponentHandSign;
    private final ObjectProperty<Image> opponentImgProperty;
    private final ProgressButton rock;
    private final ProgressButton paper;
    private final ProgressButton scissors;

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats) {
        this(gameContext, stats, -1);
    }

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;

        if (gameSeed < 0) {
            this.random = new ReplayablePseudoRandom();
            this.stats.setGameSeed(random.getSeed());
        } else {
            this.random = new ReplayablePseudoRandom(gameSeed);
        }

        widthProperty = gameContext.getRoot().widthProperty();
        heightProperty = gameContext.getRoot().heightProperty();

        layout = new Group();

        final Rectangle background = new Rectangle(0, 0, 10, 10);
        background.setFill(new ImagePattern(new Image("data/rockPaperScissors/park.png")));
        background.setMouseTransparent(true);
        background.widthProperty().bind(widthProperty);
        background.heightProperty().bind(heightProperty);

        playerScore = 0;
        opponentScore = 0;

        final Text playerScoreText = new Text(0, 0, "Your score: " + playerScore);
        playerScoreText.setFill(Color.BLACK);
        playerScoreText.setTextAlignment(TextAlignment.LEFT);
        playerScoreText.setFont(new Font(50));

        final Text opponentScoreText = new Text(0, 0, "Opponent score: " + playerScore);
        opponentScoreText.layoutXProperty().bind(widthProperty.subtract(opponentScoreText.wrappingWidthProperty()));
        opponentScoreText.setFill(Color.BLACK);
        opponentScoreText.setTextAlignment(TextAlignment.RIGHT);
        opponentScoreText.setFont(new Font(50));

        final Image opponentImg = new Image(HandSign.ROCK.getImagePath());
        final ImageView opponentView = new ImageView(opponentImg);
        final ProgressButton opponentButton = new ProgressButton(false);
        opponentButton.getButton().setVisible(false);
        opponentButton.setImage(opponentView);
        opponentView.fitWidthProperty().bind(widthProperty.divide(6));
        opponentView.fitHeightProperty().bind(widthProperty.divide(6).multiply(opponentImg.getHeight()).divide(opponentImg.getWidth()));
        opponentButton.layoutXProperty().bind(widthProperty.divide(2).subtract(opponentView.fitWidthProperty().divide(2)));
        opponentButton.layoutYProperty().bind(heightProperty.divide(4).subtract(opponentView.fitHeightProperty().divide(2)));
        final DoubleProperty minSizeProperty = opponentImg.getWidth() <= opponentImg.getHeight() ? opponentView.fitWidthProperty() : opponentView.fitHeightProperty();
        opponentButton.getButton().radiusProperty().bind(minSizeProperty.divide(2));

        rock = createRockPaperScissorsProgressButton(HandSign.ROCK);
        paper = createRockPaperScissorsProgressButton(HandSign.PAPER);
        scissors = createRockPaperScissorsProgressButton(HandSign.SCISSORS);

        layout.getChildren().addAll(background, playerScoreText, opponentScoreText, opponentButton, rock, paper, scissors);

        playerScoreTextProperty = playerScoreText.textProperty();
        opponentScoreTextProperty = opponentScoreText.textProperty();
        opponentImgProperty = opponentView.imageProperty();
    }

    @Override
    public void launch() {
        opponentHandSign = HandSign.values()[random.nextInt(HandSign.values().length)];
        opponentImgProperty.setValue(new Image(opponentHandSign.getImagePath()));

        rock.active();
        paper.active();
        scissors.active();

        gameContext.getChildren().add(layout);

        this.start();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    private ProgressButton createRockPaperScissorsProgressButton(HandSign handSign) {
        Image img = new Image(handSign.getImagePath());
        ImageView view = new ImageView(img);

        ProgressButton button = new ProgressButton(false);
        button.getButton().setVisible(false);
        button.setImage(view);

        view.fitWidthProperty().bind(widthProperty.divide(6));
        view.fitHeightProperty().bind(widthProperty.divide(6).multiply(img.getHeight()).divide(img.getWidth()));
        button.layoutXProperty().bind(widthProperty.multiply(handSign.getPosX()).subtract(view.fitWidthProperty().divide(2)));
        button.layoutYProperty().bind(heightProperty.multiply(3.0 / 4.0).subtract(view.fitHeightProperty().divide(2)));

        final DoubleProperty minSizeProperty = img.getWidth() <= img.getHeight() ? view.fitWidthProperty() : view.fitHeightProperty();
        button.getButton().radiusProperty().bind(minSizeProperty.divide(2));

        button.assignIndicatorUpdatable(event -> {
            if (handSign.figth(opponentHandSign) >= 1) {
                gameWin();
            } else {
                button.disable();
            }
            stats.incrementNumberOfGoalsReached();
        }, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(button);

        return button;
    }

    private void gameWin() {
        rock.disable();
        paper.disable();
        scissors.disable();

        playerScore = playerScore + 1;
        playerScoreTextProperty.setValue("Score: " + playerScore);

        if (playerScore == 3) {
            gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(stats, this));
            playerScore = 0;
        } else {
            gameContext.playWinTransition(0, event1 -> {
                gameContext.clear();
                launch();
            });
        }
    }

    @Override
    public void handle(final long now) {
    }

    @Override
    public void dispose() {
    }
}
