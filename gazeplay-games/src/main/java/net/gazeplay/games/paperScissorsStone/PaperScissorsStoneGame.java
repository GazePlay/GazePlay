package net.gazeplay.games.paperScissorsStone;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
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
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.components.ProgressButton;

import java.util.Random;

public class PaperScissorsStoneGame extends AnimationTimer implements GameLifeCycle {

    private final PaperScissorsStoneStats paperScissorsStoneStats;
    private final Dimension2D dimension2D;
    private final Random random;
    private final Configuration configuration;
    private final Multilinguism translate;

    private final Group backgroundLayer;
    private final Group middleLayer;
    private final Rectangle interactionOverlay;
    private final IGameContext gameContext;
    private final PaperScissorsStoneStats stats;

    private Point2D gazeTarget;

    private final Rectangle shade;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private Rectangle background;

    private ImageView ennemyI;

    private ProgressButton stone;
    private ProgressButton paper;
    private ProgressButton scissors;
    private ProgressButton ennemy;

    private enum type {paper, stone, scissors}

    private type ennemyT;

    public PaperScissorsStoneGame(final IGameContext gameContext, final PaperScissorsStoneStats stats) {
        this.stats = stats;
        this.paperScissorsStoneStats = stats;
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

        sp.getChildren().add(backgroundImage);
        backgroundImage.toBack();

        final Label onScreenText = new Label();
        foregroundLayer.getChildren().add(onScreenText);

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
        // hide end game menu
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);

        interactionOverlay.setDisable(false);

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();

        gazeTarget = new Point2D(dimension2D.getWidth() / 2, 0);

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());
        background.setFill(new ImagePattern(new Image("data/paperScissorsStone/park.png")));

        backgroundLayer.getChildren().add(background);

        stone = new ProgressButton();
        stone.setLayoutX(dimension2D.getWidth() / 6 - dimension2D.getWidth() / 12);
        stone.setLayoutY(dimension2D.getHeight() * 2 / 3);
        stone.getButton().setRadius(140);
        ImageView stoneI = new ImageView(new Image("data/paperScissorsStone/Stone.png"));
        stone.setImage(stoneI);

        stone.assignIndicator(event -> {
            if (ennemyT == type.scissors) {
                gameContext.playWinTransition(0, event1 -> {
                    gameContext.clear();
                    launch();
                    background.setFill(new ImagePattern(new Image("data/paperScissorsStone/park.png")));
                    backgroundLayer.getChildren().add(background);
                });
            } else {
                stone.disable();
            }
            stats.incrementNumberOfGoalsReached();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(stone);
        stone.active();


        paper = new ProgressButton();
        paper.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getWidth() / 12);
        paper.setLayoutY(dimension2D.getHeight() * 2 / 3);
        paper.getButton().setRadius(140);
        ImageView paperI = new ImageView(new Image("data/paperScissorsStone/Paper.png"));
        paper.setImage(paperI);

        paper.assignIndicator(event -> {
            if (ennemyT == type.stone) {
                gameContext.playWinTransition(0, event1 -> {
                    gameContext.clear();
                    launch();
                    background.setFill(new ImagePattern(new Image("data/paperScissorsStone/park.png")));
                    backgroundLayer.getChildren().add(background);
                });
            } else {
                paper.disable();
            }
            stats.incrementNumberOfGoalsReached();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(paper);
        paper.active();

        scissors = new ProgressButton();
        scissors.setLayoutX(dimension2D.getWidth() * 5 / 6 - dimension2D.getWidth() / 12);
        scissors.setLayoutY(dimension2D.getHeight() * 2 / 3);
        scissors.getButton().setRadius(140);
        ImageView scissorsI = new ImageView(new Image("data/paperScissorsStone/Scissors.png"));
        scissors.setImage(scissorsI);

        scissors.assignIndicator(event -> {
            if (ennemyT == type.paper) {
                gameContext.playWinTransition(0, event1 -> {
                    gameContext.clear();
                    launch();
                });
            } else {
                scissors.disable();
            }
            stats.incrementNumberOfGoalsReached();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(scissors);
        scissors.active();

        ennemy = new ProgressButton();
        ennemy.setLayoutX(dimension2D.getWidth() * 1 / 2 - dimension2D.getWidth() / 12);
        ennemy.setLayoutY(dimension2D.getHeight() / 5);
        ennemy.getButton().setRadius(140);
        ennemyI = new ImageView(new Image("data/paperScissorsStone/Scissors.png"));
        int i = (int) (Math.random() * (2));
        switch (i) {
            case 0:
                ennemyI = new ImageView(new Image("data/paperScissorsStone/Scissors.png"));
                ennemyT = type.scissors;
                break;
            case 1:
                ennemyI = new ImageView(new Image("data/paperScissorsStone/Paper.png"));
                ennemyT = type.paper;
                break;
            case 2:
                ennemyI = new ImageView(new Image("data/paperScissorsStone/Stone.png"));
                ennemyT = type.stone;
                break;
        }
        ennemy.setImage(ennemyI);

        middleLayer.getChildren().addAll(stone, paper, scissors, ennemy);

        gameContext.getChildren().addAll(stone, paper, scissors, ennemy);

        this.start();

        paperScissorsStoneStats.notifyNewRoundReady();
    }


    @Override
    public void handle(final long now) {

    }

    @Override
    public void dispose() {

    }
}
