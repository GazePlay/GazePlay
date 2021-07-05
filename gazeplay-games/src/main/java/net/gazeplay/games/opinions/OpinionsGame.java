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

            middleLayer.getChildren().addAll(thumbUp, thumbDown, noCare);

        }

        else {

            Oui = new ProgressButton();

            Non = new ProgressButton();

            if (type.equals(OpinionsGameVariant.ONHB)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/correct2.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, 0);
                createAddButtonOpinions(Non, "data/opinions/thumbs/error.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, dimension2D.getHeight() * 16 / 20);

            }

            else if (type.equals(OpinionsGameVariant.ONBH)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/correct2.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, dimension2D.getHeight() * 16 / 20);
                createAddButtonOpinions(Non, "data/opinions/thumbs/error.png", dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20, 0);
            }

            else if (type.equals(OpinionsGameVariant.ONGD)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/correct2.png", 0, dimension2D.getHeight() * 2 / 5);
                createAddButtonOpinions(Non, "data/opinions/thumbs/error.png", dimension2D.getWidth() * 18 / 20, dimension2D.getHeight() * 2 / 5);
            }

            else if (type.equals(OpinionsGameVariant.ONDG)) {
                createAddButtonOpinions(Oui, "data/opinions/thumbs/correct2.png", dimension2D.getWidth() * 18 / 20, dimension2D.getHeight() * 2 / 5);
                createAddButtonOpinions(Non, "data/opinions/thumbs/error.png", 0, dimension2D.getHeight() * 2 / 5);
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

        }

        gameContext.getChildren().addAll(backgroundLayer, middleLayer);

        opinionGameStats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(opinionGameStats);
    }

    private void updateScore() {
        score = score + 1;
        if (score == 10) {
            gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(opinionGameStats, this));
            middleLayer.getChildren().clear();
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
