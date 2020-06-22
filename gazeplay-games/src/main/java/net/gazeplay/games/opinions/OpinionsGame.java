package net.gazeplay.games.opinions;

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
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.components.ProgressButton;

import java.util.List;

@Slf4j
public class OpinionsGame extends AnimationTimer implements GameLifeCycle {

    private final OpinionsGameStats opinionGameStats;
    private final IGameContext gameContext;
    private final Dimension2D dimension2D;
    private final Configuration configuration;
    private final Group backgroundLayer;
    private final Group middleLayer;
    private final OpinionsGameStats stats;

    private final ImageLibrary backgroundImage;
    private final ImageLibrary thumbImage;

    private final Rectangle shade;
    private Rectangle background;
    private final ProgressButton restartButton;
    private final Text finalScoreText;

    private int score = 0;

    private ProgressButton thumbUp;
    private ProgressButton thumbDown;
    private ProgressButton noCare;

    public OpinionsGame(final IGameContext gameContext, final OpinionsGameStats stats) {
        this.stats = stats;
        this.opinionGameStats = this.stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();

        thumbImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions/thumbs"));

        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        final StackPane sp = new StackPane();
        gameContext.getChildren().addAll(sp, backgroundLayer, middleLayer, foregroundLayer);

        backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"));

        Rectangle backgroundImage = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        backgroundImage.widthProperty().bind(gameContext.getRoot().widthProperty());
        backgroundImage.heightProperty().bind(gameContext.getRoot().heightProperty());

        backgroundImage.setOpacity(0.08);

        sp.getChildren().add(backgroundImage);
        backgroundImage.toFront();

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

    }

    @Override
    public void launch() {
        // hide end game menu
        shade.setOpacity(0);
        restartButton.disable();
        finalScoreText.setOpacity(0);


        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        backgroundLayer.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        thumbDown = new ProgressButton();
        thumbDown.setLayoutX(dimension2D.getWidth() * 18 / 20);
        thumbDown.setLayoutY(dimension2D.getHeight() * 2 / 5);
        thumbDown.getButton().setRadius(70);

        ImageView thumbDo = new ImageView(new Image("data/opinions/thumbs/thumbdown.png"));
        thumbDo.setFitWidth(dimension2D.getWidth() / 10);
        thumbDo.setFitHeight(dimension2D.getHeight() / 5);
        thumbDown.setImage(thumbDo);

        thumbDown.assignIndicator(event -> {
            background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
            stats.incrementNumberOfGoalsReached();
            updateScore();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(thumbDown);
        thumbDown.active();

        noCare = new ProgressButton();
        noCare.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20);
        noCare.setLayoutY(0);
        noCare.getButton().setRadius(70);
        ImageView noCar = new ImageView(new Image("data/opinions/thumbs/nocare.png"));
        noCar.setFitWidth(dimension2D.getWidth() / 10);
        noCar.setFitHeight(dimension2D.getHeight() / 5);
        noCare.setImage(noCar);

        noCare.assignIndicator(event -> {
            background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
            stats.incrementNumberOfGoalsReached();
            updateScore();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(noCare);
        noCare.active();

        thumbUp = new ProgressButton();
        thumbUp.setLayoutX(0);
        thumbUp.setLayoutY(dimension2D.getHeight() * 2 / 5);
        thumbUp.getButton().setRadius(70);
        ImageView thumbU = new ImageView(new Image("data/opinions/thumbs/thumbup.png"));
        thumbU.setFitWidth(dimension2D.getWidth() / 10);
        thumbU.setFitHeight(dimension2D.getHeight() / 5);
        thumbUp.setImage(thumbU);

        thumbUp.assignIndicator(event -> {
            background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
            stats.incrementNumberOfGoalsReached();
            updateScore();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(thumbUp);
        thumbUp.active();

        List<Image> Picture = thumbImage.pickAllImages();
        for (Image I : Picture) {
            log.info("coucou: " + I.getUrl());
            if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbdown.png")) {
                thumbDo = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbdown.png")).getImage());
                thumbDown.setImage(thumbDo);
                thumbDo.setFitWidth(dimension2D.getWidth() / 10);
                thumbDo.setFitHeight(dimension2D.getHeight() / 5);
            }
            if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbup.png")) {
                thumbU = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbup.png")).getImage());
                thumbUp.setImage(thumbU);
                thumbU.setFitWidth(dimension2D.getWidth() / 10);
                thumbU.setFitHeight(dimension2D.getHeight() / 5);
            }
            if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/nocare.png")) {
                noCar = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/nocare.png")).getImage());
                noCare.setImage(noCar);
                noCar.setFitWidth(dimension2D.getWidth() / 10);
                noCar.setFitHeight(dimension2D.getHeight() / 5);
            }
        }

        middleLayer.getChildren().addAll(thumbUp, thumbDown, noCare);

        gameContext.getChildren().addAll(thumbUp, thumbDown, noCare);


        this.start();

        opinionGameStats.notifyNewRoundReady();
    }

    private void updateScore() {
        score = score + 1;
        if (score == 10) {
            gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(opinionGameStats, this));
        }
    }

    @Override
    public void handle(final long now) {

    }

    @Override
    public void dispose() {

    }
}
