package net.gazeplay.games.rockPaperScissors;

import javafx.animation.AnimationTimer;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.components.ProgressButton;

@Slf4j
public class RockPaperScissorsGame extends AnimationTimer implements GameLifeCycle {

    private final RockPaperScissorsStats rockPaperScissorsStats;
    private final Dimension2D dimension2D;
    private final Configuration configuration;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final IGameContext gameContext;
    private final RockPaperScissorsStats stats;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private ProgressButton rock;
    private ProgressButton paper;
    private ProgressButton scissors;
    private ProgressButton enemy;

    private int score = 0;

    private enum Type {rock, paper, scissors}

    private Type enemyType;

    private final ReplayablePseudoRandom random;

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats) {
        this.stats = stats;
        this.rockPaperScissorsStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        final StackPane sp = new StackPane();
        gameContext.getChildren().addAll(sp, backgroundLayer, middleLayer, foregroundLayer);

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
        restartButton.assignIndicatorUpdatable(event -> launch(), gameContext);

        finalScoreText = new Text(0, dimension2D.getHeight() / 4, "");
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setTextAlignment(TextAlignment.CENTER);
        finalScoreText.setFont(new Font(50));
        finalScoreText.setWrappingWidth(dimension2D.getWidth());
        foregroundLayer.getChildren().addAll(shade, finalScoreText, restartButton);

        gameContext.getGazeDeviceManager().addEventFilter(restartButton);
    }

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats, double gameSeed) {
        this.stats = stats;
        this.rockPaperScissorsStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        this.random = new ReplayablePseudoRandom(gameSeed);

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        final StackPane sp = new StackPane();
        gameContext.getChildren().addAll(sp, backgroundLayer, middleLayer, foregroundLayer);

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
        restartButton.assignIndicatorUpdatable(event -> launch(), gameContext);

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

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();

        Rectangle background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());
        ImagePattern backgroundI = new ImagePattern(new Image("data/rockPaperScissors/park.png"));
        background.setFill(backgroundI);

        rock = new ProgressButton();
        setUpStonePaperScissorsProgressButton(rock, "data/rockPaperScissors/rock.png", dimension2D.getWidth() / 6 - dimension2D.getWidth() / 12);

        paper = new ProgressButton();
        setUpStonePaperScissorsProgressButton(paper, "data/rockPaperScissors/paper.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 12);

        scissors = new ProgressButton();
        setUpStonePaperScissorsProgressButton(scissors, "data/rockPaperScissors/scissors.png", dimension2D.getWidth() * 5 / 6 - dimension2D.getWidth() / 12);

        enemy = new ProgressButton();
        enemy.setLayoutX(dimension2D.getWidth() * 1 / 2 - dimension2D.getWidth() / 12);
        enemy.setLayoutY(dimension2D.getHeight() / 5);
        enemy.getButton().setRadius(100);
        enemyType = Type.values()[random.nextInt(Type.values().length)];
        ImageView enemyImg = new ImageView(new Image("data/rockPaperScissors/" + enemyType + ".png"));
        enemy.setImage(enemyImg);

        backgroundLayer.getChildren().add(background);
        middleLayer.getChildren().addAll(rock, paper, scissors, enemy);

        gameContext.getChildren().addAll(background, rock, paper, scissors, enemy);

        this.start();

        rockPaperScissorsStats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(rockPaperScissorsStats);
    }

    public void setUpStonePaperScissorsProgressButton(ProgressButton button, String imageLink, double posX) {
        button.setLayoutX(posX);
        button.setLayoutY(dimension2D.getHeight() * 2 / 3);
        button.getButton().setRadius(100);
        ImageView stoneI = new ImageView(new Image(imageLink));
        button.setImage(stoneI);

        if (button == paper) {
            button.assignIndicatorUpdatable(event -> {
                if (enemyType == Type.rock) {
                    gameWin();
                } else {
                    button.disable(true);
                }
                stats.incrementNumberOfGoalsReached();
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(paper);
            button.active();
        }
        if (button == rock) {
            button.assignIndicatorUpdatable(event -> {
                if (enemyType == Type.scissors) {
                    gameWin();
                } else {
                    button.disable(true);
                }
                stats.incrementNumberOfGoalsReached();
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(rock);
            button.active();
        }
        if (button == scissors) {
            button.assignIndicatorUpdatable(event -> {
                if (enemyType == Type.paper) {
                    gameWin();
                } else {
                    button.disable(true);
                }
                stats.incrementNumberOfGoalsReached();
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(scissors);
            button.active();
        }

    }

    private void gameWin() {
        score = score + 1;
        rock.disable(true);
        paper.disable(true);
        scissors.disable(true);
        if (score == 3) {
            gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(stats, this));
            score = 0;
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
