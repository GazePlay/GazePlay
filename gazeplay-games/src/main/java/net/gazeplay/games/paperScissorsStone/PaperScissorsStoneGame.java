package net.gazeplay.games.paperScissorsStone;

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
public class PaperScissorsStoneGame extends AnimationTimer implements GameLifeCycle {

    private final PaperScissorsStoneStats paperScissorsStoneStats;
    private final Dimension2D dimension2D;
    private final Configuration configuration;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final IGameContext gameContext;
    private final PaperScissorsStoneStats stats;
    private final boolean inReplayMode;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private ProgressButton stone;
    private ProgressButton paper;
    private ProgressButton scissors;
    private ProgressButton ennemy;

    private int score = 0;

    private enum Type {paper, stone, scissors}

    private Type ennemyT;

    private final ReplayablePseudoRandom random;

    public PaperScissorsStoneGame(final IGameContext gameContext, final PaperScissorsStoneStats stats) {
        this.stats = stats;
        this.paperScissorsStoneStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        this.random = new ReplayablePseudoRandom();
        this.stats.setCurrentGameSeed(random.getSeed());
        this.inReplayMode = false;

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

    public PaperScissorsStoneGame(final IGameContext gameContext, final PaperScissorsStoneStats stats, double gameSeed) {
        this.stats = stats;
        this.paperScissorsStoneStats = stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        this.random = new ReplayablePseudoRandom(gameSeed);
        this.inReplayMode = true;

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
        ImagePattern backgroundI = new ImagePattern(new Image("data/paperScissorsStone/park.png"));
        background.setFill(backgroundI);

        stone = new ProgressButton();
        setUpStonePaperScissorsProgressButton(stone, "data/paperScissorsStone/Stone.png", dimension2D.getWidth() / 6 - dimension2D.getWidth() / 12);

        paper = new ProgressButton();
        setUpStonePaperScissorsProgressButton(paper, "data/paperScissorsStone/Paper.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 12);

        scissors = new ProgressButton();
        setUpStonePaperScissorsProgressButton(scissors, "data/paperScissorsStone/Scissors.png", dimension2D.getWidth() * 5 / 6 - dimension2D.getWidth() / 12);

        ennemy = new ProgressButton();
        ennemy.setLayoutX(dimension2D.getWidth() * 1 / 2 - dimension2D.getWidth() / 12);
        ennemy.setLayoutY(dimension2D.getHeight() / 5);
        ennemy.getButton().setRadius(100);
        ImageView ennemyI = new ImageView(new Image("data/paperScissorsStone/Scissors.png"));
        int i = random.nextInt(3);
        switch (i) {
            case 0:
                ennemyI = new ImageView(new Image("data/paperScissorsStone/Scissors.png"));
                ennemyT = Type.scissors;
                break;
            case 1:
                ennemyI = new ImageView(new Image("data/paperScissorsStone/Paper.png"));
                ennemyT = Type.paper;
                break;
            case 2:
                ennemyI = new ImageView(new Image("data/paperScissorsStone/Stone.png"));
                ennemyT = Type.stone;
                break;
            default:
                log.info("out of bounds");
        }
        ennemy.setImage(ennemyI);

        backgroundLayer.getChildren().add(background);
        middleLayer.getChildren().addAll(stone, paper, scissors, ennemy);

        gameContext.getChildren().addAll(background, stone, paper, scissors, ennemy);

        this.start();

        paperScissorsStoneStats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(paperScissorsStoneStats);
    }

    public void setUpStonePaperScissorsProgressButton(ProgressButton button, String imageLink, double posX) {
        button.setLayoutX(posX);
        button.setLayoutY(dimension2D.getHeight() * 2 / 3);
        button.getButton().setRadius(100);
        ImageView stoneI = new ImageView(new Image(imageLink));
        button.setImage(stoneI);

        if (button == paper) {
            button.assignIndicatorUpdatable(event -> {
                if (ennemyT == Type.stone) {
                    gameWin();
                } else {
                    button.disable(true);
                }
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(paper);
            button.active();
        }
        if (button == stone) {
            button.assignIndicatorUpdatable(event -> {
                if (ennemyT == Type.scissors) {
                    gameWin();
                } else {
                    button.disable(true);
                }
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(stone);
            button.active();
        }
        if (button == scissors) {
            button.assignIndicatorUpdatable(event -> {
                if (ennemyT == Type.paper) {
                    gameWin();
                } else {
                    button.disable(true);
                }
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(scissors);
            button.active();
        }

    }

    private void gameWin() {
        score = score + 1;
        stone.disable(true);
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
