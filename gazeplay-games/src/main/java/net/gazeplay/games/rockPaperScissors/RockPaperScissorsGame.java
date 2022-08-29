package net.gazeplay.games.rockPaperScissors;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.components.ProgressButton;

@Slf4j
public class RockPaperScissorsGame extends AnimationTimer implements GameLifeCycle {

    private final IGameContext gameContext;
    private final RockPaperScissorsStats stats;
    private final RockPaperScissorsGameVariant gameVariant;
    private final Configuration configuration;
    private final Multilinguism multilinguism;
    private final ReplayablePseudoRandom random;

    private final ReadOnlyDoubleProperty widthProperty;
    private final ReadOnlyDoubleProperty heightProperty;

    private final IntegerProperty playerScore;
    private final IntegerProperty opponentScore;
    private final ObjectProperty<Image> opponentImage;

    private final Group layout;
    private final ProgressButton rockButton;
    private final ProgressButton paperButton;
    private final ProgressButton scissorsButton;

    private HandSign opponentHandSign;

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats, EnumGameVariant<RockPaperScissorsGameVariant> gameVariant) {
        this(gameContext, stats, gameVariant, -1);
    }

    public RockPaperScissorsGame(final IGameContext gameContext, final RockPaperScissorsStats stats, EnumGameVariant<RockPaperScissorsGameVariant> gameVariant, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant.getEnumValue();
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
        opponentImage = new SimpleObjectProperty<>();

        layout = new Group();

        final Rectangle background = new Rectangle(0, 0, 10, 10);
        background.setFill(new ImagePattern(new Image("data/rockPaperScissors/park.png")));
        background.setMouseTransparent(true);
        background.widthProperty().bind(widthProperty);
        background.heightProperty().bind(heightProperty);

        final GridPane textsGridPane = createTextsGridPane();

        final Image image = HandSign.UNKNOWN.getImage();
        final ImageView opponentView = new ImageView(image);
        final ProgressButton opponentButton = new ProgressButton(false);
        opponentButton.getButton().setVisible(false);
        opponentButton.setImage(opponentView);
        opponentView.imageProperty().bind(opponentImage);
        opponentView.fitWidthProperty().bind(widthProperty.divide(6));
        opponentView.fitHeightProperty().bind(widthProperty.divide(6).multiply(image.getHeight()).divide(image.getWidth()));
        opponentButton.layoutXProperty().bind(widthProperty.divide(2).subtract(opponentView.fitWidthProperty().divide(2)));
        opponentButton.layoutYProperty().bind(heightProperty.divide(4).subtract(opponentView.fitHeightProperty().divide(2)));

        rockButton = createRockPaperScissorsProgressButton(HandSign.ROCK);
        paperButton = createRockPaperScissorsProgressButton(HandSign.PAPER);
        scissorsButton = createRockPaperScissorsProgressButton(HandSign.SCISSORS);

        layout.getChildren().addAll(background, textsGridPane, opponentButton, rockButton, paperButton, scissorsButton);
    }

    @Override
    public void launch() {
        opponentHandSign = gameVariant.isVisible() ? HandSign.values()[random.nextInt(3) + 1] : HandSign.UNKNOWN;
        opponentImage.setValue(opponentHandSign.getImage());

        rockButton.active();
        paperButton.active();
        scissorsButton.active();

        gameContext.getChildren().add(layout);

        this.start();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    private GridPane createTextsGridPane() {
        final String lang = configuration.getLanguage();
        final String userName = configuration.getUserName();

        final String playoffLabel = gameVariant.getLabel();

        final String playoffString = multilinguism.getTranslation(playoffLabel, lang);
        final String playerNameString = userName.equals("") ? multilinguism.getTranslation("Player", lang) : userName;
        final String opponentNameString = multilinguism.getTranslation("Opponent", lang);
        final String scoresString = multilinguism.getTranslation("Scores", lang);
        final String colonString = multilinguism.getTranslation("Colon", lang);

        final Text playoffText = new Text(playoffString);
        final Text playerNameText = new Text(playerNameString);
        final Text slash1Text = new Text("/");
        final Text opponentNameText = new Text(opponentNameString);
        final Text scoresText = new Text(scoresString + colonString);
        final Text playerScoreText = new Text();
        final Text slash2Text = new Text("/");
        final Text opponentScoreText = new Text();

        final Font font = new Font(35);

        playoffText.setFont(font);
        playerNameText.setFont(font);
        slash1Text.setFont(font);
        opponentNameText.setFont(font);
        scoresText.setFont(font);
        playerScoreText.setFont(font);
        slash2Text.setFont(font);
        opponentScoreText.setFont(font);

        playerScoreText.textProperty().bind(playerScore.asString());
        opponentScoreText.textProperty().bind(opponentScore.asString());

        final GridPane textsGridPane = new GridPane();
        textsGridPane.setPadding(new Insets(5, 0, 0, 10));
        textsGridPane.setHgap(15);

        textsGridPane.add(playoffText, 0, 0, 5, 1);
        textsGridPane.add(playerNameText, 1, 1);
        textsGridPane.add(slash1Text, 2, 1);
        textsGridPane.add(opponentNameText, 3, 1);
        textsGridPane.add(scoresText, 0, 2);
        textsGridPane.add(playerScoreText, 1, 2);
        textsGridPane.add(slash2Text, 2, 2);
        textsGridPane.add(opponentScoreText, 3, 2);

        GridPane.setHalignment(playoffText, HPos.LEFT);
        GridPane.setHalignment(playerNameText, HPos.RIGHT);
        GridPane.setHalignment(slash1Text, HPos.CENTER);
        GridPane.setHalignment(opponentNameText, HPos.LEFT);
        GridPane.setHalignment(scoresText, HPos.RIGHT);
        GridPane.setHalignment(playerScoreText, HPos.RIGHT);
        GridPane.setHalignment(slash2Text, HPos.CENTER);
        GridPane.setHalignment(opponentScoreText, HPos.LEFT);

        return textsGridPane;
    }

    private ProgressButton createRockPaperScissorsProgressButton(HandSign handSign) {
        Image image = handSign.getImage();
        ImageView view = new ImageView(image);

        ProgressButton button = new ProgressButton(false);
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
                if (handSign.fight(opponentHandSign) >= 1) {
                    rockButton.disable();
                    paperButton.disable();
                    scissorsButton.disable();
                    roundWin();
                } else {
                    button.disable();
                }
            } else {
                opponentHandSign = HandSign.values()[random.nextInt(3) + 1];
                opponentImage.setValue(opponentHandSign.getImage());

                if (!button.equals(rockButton)) {
                    rockButton.disable();
                }
                if (!button.equals(paperButton)) {
                    paperButton.disable();
                }
                if (!button.equals(scissorsButton)) {
                    scissorsButton.disable();
                }

                switch (handSign.fight(opponentHandSign)) {
                    case -1 -> roundLoss();
                    case 0 -> newRound();
                    case 1 -> roundWin();
                }
            }
            stats.incrementNumberOfGoalsReached();
        }, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(button);

        return button;
    }

    private void newRound() {
        gameContext.clear();
        launch();
    }

    private void roundWin() {
        playerScore.setValue(playerScore.getValue() + 1);

        if (playerScore.getValue() == gameVariant.getNbRounds()) {
            gameContext.playWinTransition(0, event -> gameEnd());
        } else {
            if (gameVariant.isVisible()) {
                gameContext.playWinTransition(0, event -> newRound());
            } else {
                // display thumbs-up -> at end, do:
                newRound();
            }
        }
    }

    private void roundLoss() {
        opponentScore.setValue(opponentScore.getValue() + 1);

        if (opponentScore.getValue() == gameVariant.getNbRounds()) {
            gameEnd();
        } else {
            // display thumbs-down -> at end, do:
            newRound();
        }
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
