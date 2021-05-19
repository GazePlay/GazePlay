package net.gazeplay.games.opinions;

import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.components.ProgressButton;
import net.gazeplay.games.cakes.CakeGameVariant;
import org.w3c.dom.css.Rect;

import java.util.List;

@Slf4j
public class OpinionsGame implements GameLifeCycle {

    private final OpinionsGameStats opinionGameStats;
    private final IGameContext gameContext;
    private final Dimension2D dimension2D;
    private final Configuration configuration;
    private final Group backgroundLayer;
    private final Group middleLayer;
    private final OpinionsGameStats stats;

    private final ImageLibrary backgroundImage;
    private final ImageLibrary thumbImage;

    private Rectangle background;

    private ProgressButton thumbDown;
    private ProgressButton noCare;
    private ProgressButton thumbUp;

    private ProgressButton Oui;
    private ProgressButton Non;

    private int score = 0;

    private final ReplayablePseudoRandom randomGenerator;

    private final OpinionsGameVariant type;

    public OpinionsGame(final IGameContext gameContext, final OpinionsGameStats stats, final OpinionsGameVariant type) {
        this.stats = stats;
        this.opinionGameStats = this.stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        thumbImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions/thumbs"), randomGenerator);
        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"), randomGenerator);
        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        gameContext.getChildren().add(foregroundLayer);

        this.type = type;

    }

    public OpinionsGame(final IGameContext gameContext, final OpinionsGameStats stats, final OpinionsGameVariant type,  double gameSeed) {
        this.stats = stats;
        this.opinionGameStats = this.stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        thumbImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions/thumbs"), randomGenerator);
        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"), randomGenerator);
        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        gameContext.getChildren().add(foregroundLayer);

        this.type = type;

    }

    @Override
    public void launch() {

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        backgroundLayer.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        if (type.equals(OpinionsGameVariant.OPINIONS)) {

            thumbDown = new ProgressButton();
            createAddButtonOpinions(thumbDown, "data/opinions/thumbs/thumbdown.png", dimension2D.getWidth() * 18 / 20, dimension2D.getHeight() * 2 / 5);

            thumbDown.assignIndicator(event -> {
                background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
                stats.incrementNumberOfGoalsReached();
                updateScore();
            }, configuration.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(thumbDown);
            thumbDown.active();

            noCare = new ProgressButton();
            createAddButtonOpinions(noCare, "data/opinions/thumbs/nocare.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, 0);

            noCare.assignIndicator(event -> {
                background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
                stats.incrementNumberOfGoalsReached();
                updateScore();
            }, configuration.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(noCare);
            noCare.active();

            thumbUp = new ProgressButton();
            createAddButtonOpinions(thumbUp, "data/opinions/thumbs/thumbup.png", 0, dimension2D.getHeight() * 2 / 5);

            thumbUp.assignIndicator(event -> {
                background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
                stats.incrementNumberOfGoalsReached();
                updateScore();
            }, configuration.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(thumbUp);
            thumbUp.active();

            /*List<Image> Picture = thumbImage.pickAllImages();
            for (Image I : Picture) {
                log.info("coucou: " + I.getUrl());
                if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbdown.png")) {
                    ImageView thumbDo = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbdown.png")).getImage());
                    thumbDown.setImage(thumbDo);
                    thumbDo.setFitWidth(dimension2D.getWidth() / 10);
                    thumbDo.setFitHeight(dimension2D.getHeight() / 5);
                }
                if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbup.png")) {
                    ImageView thumbU = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbup.png")).getImage());
                    thumbUp.setImage(thumbU);
                    thumbU.setFitWidth(dimension2D.getWidth() / 10);
                    thumbU.setFitHeight(dimension2D.getHeight() / 5);
                }
                if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/nocare.png")) {
                    ImageView noCar = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/nocare.png")).getImage());
                    noCare.setImage(noCar);
                    noCar.setFitWidth(dimension2D.getWidth() / 10);
                    noCar.setFitHeight(dimension2D.getHeight() / 5);
                }
            }*/

            middleLayer.getChildren().addAll(thumbUp, thumbDown, noCare);

        }

        else {

            Oui = new ProgressButton();

            Non = new ProgressButton();

            Rectangle bar = new Rectangle();

            if (type.equals(OpinionsGameVariant.ONHB)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/correct2.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, 0);
                createAddButtonOpinions(Non, "data/opinions/thumbs/error.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, dimension2D.getHeight() * 16 / 20);
                bar = new Rectangle(dimension2D.getWidth() * 0.05, dimension2D.getHeight() * 0.475, dimension2D.getWidth() * 0.9, dimension2D.getHeight() * 0.05);
                bar.setFill(new ImagePattern(new Image("data/opinions/thumbs/etagere.png")));
            }

            if (type.equals(OpinionsGameVariant.ONBH)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/oui.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, dimension2D.getHeight() * 16 / 20);
                createAddButtonOpinions(Non, "data/opinions/thumbs/non.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, 0);
                bar = new Rectangle(dimension2D.getWidth() * 0.05, dimension2D.getHeight() * 0.475, dimension2D.getWidth() * 0.9, dimension2D.getHeight() * 0.05);
                bar.setFill(new ImagePattern(new Image("data/opinions/thumbs/etagere.png")));
            }

            if (type.equals(OpinionsGameVariant.ONGD)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/oui.png", 0, dimension2D.getHeight() * 2 / 5);
                createAddButtonOpinions(Non, "data/opinions/thumbs/non.png", dimension2D.getWidth() * 18 / 20, dimension2D.getHeight() * 2 / 5);
                bar = new Rectangle(dimension2D.getWidth() * 0.475, dimension2D.getHeight() * 0.05, dimension2D.getWidth() * 0.05, dimension2D.getHeight() * 0.9);
                bar.setFill(new ImagePattern(new Image("data/opinions/thumbs/etagererot.png")));
            }

            if (type.equals(OpinionsGameVariant.ONDG)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/oui.png", dimension2D.getWidth() * 18 / 20, dimension2D.getHeight() * 2 / 5);
                createAddButtonOpinions(Non, "data/opinions/thumbs/non.png", 0, dimension2D.getHeight() * 2 / 5);
                bar = new Rectangle(dimension2D.getWidth() * 0.475, dimension2D.getHeight() * 0.05, dimension2D.getWidth() * 0.05, dimension2D.getHeight() * 0.9);
                bar.setFill(new ImagePattern(new Image("data/opinions/thumbs/etagererot.png")));
            }

            Oui.assignIndicator(event -> {
                background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
                stats.incrementNumberOfGoalsReached();
                updateScore();
            }, configuration.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(Oui);
            Oui.active();

            Non.assignIndicator(event -> {
                background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
                stats.incrementNumberOfGoalsReached();
                updateScore();
            }, configuration.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(Non);
            Non.active();

            middleLayer.getChildren().addAll(Oui, Non);

            backgroundLayer.getChildren().add(bar);

        }

        gameContext.getChildren().addAll(backgroundLayer, middleLayer);

        opinionGameStats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(opinionGameStats);
    }

    private void updateScore() {
        score = score + 1;
        if (score == 10) {
            gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(opinionGameStats, this));
            thumbUp.disable(true);
            thumbDown.disable(true);
            noCare.disable(true);
            Oui.disable(true);
            Non.disable(true);
            score = 0;
        }
    }

    private void createAddButtonOpinions(ProgressButton button, String link, double setLayoutX, double setLayoutY) {
        button.setLayoutX(setLayoutX);
        button.setLayoutY(setLayoutY);
        button.getButton().setRadius(70);

        ImageView buttonI = new ImageView(new Image(link));
        buttonI.setFitWidth(dimension2D.getWidth() / 10);
        buttonI.setFitHeight(dimension2D.getHeight() / 5);
        button.setImage(buttonI);
    }

    @Override
    public void dispose() {

    }
}
