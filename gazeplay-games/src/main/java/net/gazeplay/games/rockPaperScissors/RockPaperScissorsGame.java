package net.gazeplay.games.rockPaperScissors;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.components.ProgressButton;

@Slf4j
public class RockPaperScissorsGame extends AnimationTimer implements GameLifeCycle {

    private final IGameContext gameContext;
    private final RockPaperScissorsGameVariant gameVariant;
    private final RockPaperScissorsStats stats;
    private final Configuration configuration;
    private final Multilinguism multilinguism;
    private final ReplayablePseudoRandom random;

    private final ReadOnlyDoubleProperty widthProperty;
    private final ReadOnlyDoubleProperty heightProperty;

    private final IntegerProperty playerScore;
    private final IntegerProperty opponentScore;
    private final ObjectProperty<HandSign> opponentHandSign;
    private final BooleanProperty fightWin;
    private final BooleanProperty thumbSignalVisible;

    private final Group layout;
    private final ProgressButton rockButton;
    private final ProgressButton paperButton;
    private final ProgressButton scissorsButton;
    private final SequentialTransition winLossAnimation;
    private final PauseTransition drawAnimation;

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats, RockPaperScissorsGameVariant gameVariant) {
        this(gameContext, stats, gameVariant, -1);
    }

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats, RockPaperScissorsGameVariant gameVariant, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.configuration = gameContext.getConfiguration();
        this.multilinguism = MultilinguismFactory.getSingleton();

        if (gameSeed < 0) {
            this.random = new ReplayablePseudoRandom();
            this.stats.setGameSeed(random.getSeed());
        } else {
            this.random = new ReplayablePseudoRandom(gameSeed);
        }

        widthProperty = gameContext.getRoot().widthProperty();
        heightProperty = gameContext.getRoot().heightProperty();

        playerScore = new SimpleIntegerProperty(0);
        opponentScore = new SimpleIntegerProperty(0);
        opponentHandSign = new SimpleObjectProperty<>(HandSign.UNKNOWN);
        fightWin = new SimpleBooleanProperty();
        thumbSignalVisible = new SimpleBooleanProperty(false);

        layout = new Group();

        createBackground();
        final GridPane scoreTexts = createScoreTexts();
        final Rectangle opponentButton = createOpponentHandSign();
        final Rectangle thumbSignal = createThumbSignal();
        rockButton = createPlayerHandSigns(HandSign.ROCK);
        paperButton = createPlayerHandSigns(HandSign.PAPER);
        scissorsButton = createPlayerHandSigns(HandSign.SCISSORS);
        winLossAnimation = createWinLossAnimation(thumbSignal);
        drawAnimation = createDrawAnimation();

        layout.getChildren().addAll(scoreTexts, opponentButton, rockButton, paperButton, scissorsButton, thumbSignal);
    }

    @Override
    public void launch() {
        gameContext.clear();

        thumbSignalVisible.setValue(false);
        opponentHandSign.setValue(gameVariant.isVisible() ? HandSign.values()[random.nextInt(3)] : HandSign.UNKNOWN);

        rockButton.active();
        paperButton.active();
        scissorsButton.active();

        gameContext.getChildren().add(layout);

        this.start();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    private void createBackground() {
        Background background = new Background(new BackgroundImage(
            new Image("data/rockPaperScissors/park.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
        ));
        gameContext.getRoot().setBackground(background);
    }

    private GridPane createScoreTexts() {
        final String lang = configuration.getLanguage();
        final String userName = configuration.getUserName();

        final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());
        heightProperty.addListener((observableValue, oldHeight, newHeight) -> font.set(Font.font(newHeight.doubleValue() / 25)));

        final GridPane textsGridPane = new GridPane();
        textsGridPane.setPadding(new Insets(5, 0, 0, 10));
        textsGridPane.setHgap(15);

        final String scoresString = multilinguism.getTranslation("Scores", lang);
        final String colonString = multilinguism.getTranslation("Colon", lang);
        final Text scoresText = new Text(scoresString + colonString);
        scoresText.fontProperty().bind(font);
        textsGridPane.add(scoresText, 0, gameVariant.isVisible() ? 0 : 2);

        final Text playerScoreText = new Text();
        playerScoreText.fontProperty().bind(font);
        playerScoreText.textProperty().bind(playerScore.asString());
        GridPane.setHalignment(playerScoreText, gameVariant.isVisible() ? HPos.LEFT : HPos.RIGHT);
        textsGridPane.add(playerScoreText, 1, gameVariant.isVisible() ? 0 : 2);

        final Text slash2Text = new Text("/");
        slash2Text.fontProperty().bind(font);
        textsGridPane.add(slash2Text, 2, gameVariant.isVisible() ? 0 : 2);

        final Text opponentScoreText = new Text(Integer.toString(gameVariant.getNbRounds()));
        opponentScoreText.fontProperty().bind(font);
        textsGridPane.add(opponentScoreText, 3, gameVariant.isVisible() ? 0 : 2);

        if (!gameVariant.isVisible()) {
            opponentScoreText.textProperty().bind(opponentScore.asString());

            final String playoffString = multilinguism.getTranslation(gameVariant.getLabel(), lang);
            final Text playoffText = new Text(playoffString);
            playoffText.fontProperty().bind(font);
            textsGridPane.add(playoffText, 0, 0, 5, 1);

            final String playerNameString = userName.equals("") ? multilinguism.getTranslation("Player", lang) : userName;
            final Text playerNameText = new Text(playerNameString);
            playerNameText.fontProperty().bind(font);
            textsGridPane.add(playerNameText, 1, 1);

            final String opponentNameString = multilinguism.getTranslation("Opponent", lang);
            final Text opponentNameText = new Text(opponentNameString);
            opponentNameText.fontProperty().bind(font);
            textsGridPane.add(opponentNameText, 3, 1);

            final Text slash1Text = new Text("/");
            slash1Text.fontProperty().bind(font);
            textsGridPane.add(slash1Text, 2, 1);
        }

        return textsGridPane;
    }

    private Rectangle createOpponentHandSign() {
        final Image image = HandSign.UNKNOWN.getImage();
        final Rectangle rectangle = new Rectangle();
        rectangle.setFill(new ImagePattern(image));
        rectangle.fillProperty().bind(
            new When(opponentHandSign.isEqualTo(HandSign.ROCK))
                .then(new ImagePattern(HandSign.ROCK.getImage()))
                .otherwise(new When(opponentHandSign.isEqualTo(HandSign.PAPER))
                    .then(new ImagePattern(HandSign.PAPER.getImage()))
                    .otherwise(new When(opponentHandSign.isEqualTo(HandSign.SCISSORS))
                        .then(new ImagePattern(HandSign.SCISSORS.getImage()))
                        .otherwise(new ImagePattern(HandSign.UNKNOWN.getImage())))));
        rectangle.widthProperty().bind(widthProperty.divide(6));
        rectangle.heightProperty().bind(widthProperty.divide(6).multiply(image.getHeight()).divide(image.getWidth()));
        rectangle.layoutXProperty().bind(widthProperty.divide(2).subtract(rectangle.widthProperty().divide(2)));
        rectangle.layoutYProperty().bind(heightProperty.divide(4).subtract(rectangle.heightProperty().divide(2)));
        return rectangle;
    }

    private ProgressButton createPlayerHandSigns(HandSign handSign) {
        final Image image = handSign.getImage();
        final ImageView view = new ImageView(image);

        final ProgressButton button = new ProgressButton(false);
        button.getButton().setVisible(false);
        button.setImage(view);

        view.fitWidthProperty().bind(widthProperty.divide(6));
        view.fitHeightProperty().bind(widthProperty.divide(6).multiply(image.getHeight()).divide(image.getWidth()));
        button.layoutXProperty().bind(widthProperty.multiply(handSign.getPosX()).subtract(view.fitWidthProperty().divide(2)));
        button.layoutYProperty().bind(heightProperty.multiply(3.0 / 4.0).subtract(view.fitHeightProperty().divide(2)));

        final DoubleProperty minSizeProperty = image.getWidth() <= image.getHeight() ? view.fitWidthProperty() : view.fitHeightProperty();
        button.getButton().radiusProperty().bind(minSizeProperty.divide(3));

        button.assignIndicatorUpdatable(event -> {
            if (gameVariant.isVisible()) {
                if (handSign.fight(opponentHandSign.getValue()) >= 1) {
                    rockButton.disable();
                    paperButton.disable();
                    scissorsButton.disable();
                    roundWin();
                } else {
                    button.disable();
                }
            } else {
                opponentHandSign.setValue(HandSign.values()[random.nextInt(3)]);

                if (!button.equals(rockButton)) {
                    rockButton.disable();
                }
                if (!button.equals(paperButton)) {
                    paperButton.disable();
                }
                if (!button.equals(scissorsButton)) {
                    scissorsButton.disable();
                }

                switch (handSign.fight(opponentHandSign.getValue())) {
                    case 1 -> roundWin();
                    case -1 -> roundLoss();
                    default -> roundDraw();
                }
            }
            stats.incrementNumberOfGoalsReached();
        }, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(button);

        return button;
    }

    private Rectangle createThumbSignal() {
        final Image image = new Image("data/rockPaperScissors/thumbs-up.png");
        final Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.setMouseTransparent(true);
        rectangle.setFill(new ImagePattern(image));
        rectangle.fillProperty().bind(new When(fightWin)
            .then(new ImagePattern(new Image("data/rockPaperScissors/thumbs-up.png")))
            .otherwise(new ImagePattern(new Image("data/rockPaperScissors/thumbs-down.png"))));
        rectangle.visibleProperty().bind(thumbSignalVisible);
        rectangle.widthProperty().bind(heightProperty.divide(2).multiply(image.getWidth()).divide(image.getHeight()));
        rectangle.heightProperty().bind(heightProperty.divide(2));
        rectangle.layoutXProperty().bind(widthProperty.divide(2).subtract(rectangle.widthProperty().divide(2)));
        rectangle.layoutYProperty().bind(heightProperty.divide(2).subtract(rectangle.heightProperty().divide(2)));
        return rectangle;
    }

    private SequentialTransition createWinLossAnimation(Rectangle rectangle) {
        final ScaleTransition transition1 = new ScaleTransition(Duration.millis(1000), rectangle);
        transition1.setFromX(0);
        transition1.setFromY(0);
        transition1.setToX(1.2);
        transition1.setToY(1.2);
        final ScaleTransition transition2 = new ScaleTransition(Duration.millis(1000), rectangle);
        transition2.setFromX(1.2);
        transition2.setFromY(1.2);
        transition2.setToX(0.3);
        transition2.setToY(0.3);
        final ScaleTransition transition3 = new ScaleTransition(Duration.millis(1000), rectangle);
        transition3.setFromX(0.3);
        transition3.setFromY(0.3);
        transition3.setToX(1);
        transition3.setToY(1);
        final SequentialTransition fullTransition = new SequentialTransition(transition1, transition2, transition3);
        fullTransition.setOnFinished(event -> {
            if (opponentScore.getValue() == gameVariant.getNbRounds()) {
                gameEnd();
            } else {
                launch();
            }
        });
        return fullTransition;
    }

    private PauseTransition createDrawAnimation() {
        final PauseTransition transition = new PauseTransition(Duration.millis(1500));
        transition.setOnFinished(event -> launch());
        return transition;
    }

    private void roundWin() {
        playerScore.setValue(playerScore.getValue() + 1);

        if (playerScore.getValue() == gameVariant.getNbRounds()) {
            gameContext.playWinTransition(0, event -> gameEnd());
        } else {
            if (gameVariant.isVisible()) {
                gameContext.playWinTransition(0, event -> launch());
            } else {
                fightWin.setValue(true);
                thumbSignalVisible.setValue(true);
                winLossAnimation.play();
            }
        }
    }

    private void roundDraw() {
        drawAnimation.play();
    }

    private void roundLoss() {
        opponentScore.setValue(opponentScore.getValue() + 1);

        fightWin.setValue(false);
        thumbSignalVisible.setValue(true);
        winLossAnimation.play();
    }

    private void gameEnd() {
        playerScore.setValue(0);
        opponentScore.setValue(0);
        gameContext.showRoundStats(stats, this);
    }

    @Override
    public void handle(final long now) {
    }

    @Override
    public void dispose() {
    }
}
