package net.gazeplay.games.goosegame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.DiceRoller;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressButton;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

@Slf4j
public class GooseGame implements GameLifeCycle {

    private static final String BIBOULEPATH = "data/biboulejump/biboules/%s.png";

    private final IGameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final int nbPlayers;

    private final ImageView boardImage;
    private final ArrayList<String> bibouleColors;

    private final ArrayList<DiceRoller> diceRollers;
    private final Timeline moveDiceIn;
    private final Timeline moveDiceOut;
    private final GridPane diceDisplay;
    private final int[] rolls;
    private ProgressButton rollButton;

    private final VBox messages;

    private final ArrayList<Pawn> pawns;
    private int currentPawn;
    private Square firstSquare;

    private final ImageView turnIndicator;
    private final Timeline showPlayingBiboule;

    private final Random random;

    public GooseGame(final IGameContext gameContext, final Stats stats, final int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Configuration config = gameContext.getConfiguration();

        this.random = new Random();

        // JSON file used to store the position of each square, later used for pawn movement
        final JsonParser parser = new JsonParser();
        final JsonArray positions = (JsonArray) parser.parse(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("data/goosegame/positions.json")),
            StandardCharsets.UTF_8));

        boardImage = new ImageView("data/goosegame/gooseboard.png");
        // The board is scaled according to the window size, this influences the position we got above, so we need to
        // scale those too
        final double scaleRatio = Math.min((dimensions.getHeight() * 0.9) / boardImage.getImage().getHeight(),
            (dimensions.getWidth() * 0.9) / boardImage.getImage().getWidth());
        final double boardWidth = boardImage.getImage().getWidth() * scaleRatio;
        final double boardHeight = boardImage.getImage().getHeight() * scaleRatio;
        boardImage.setFitHeight(boardHeight);
        boardImage.setFitWidth(boardWidth);
        // Board is centered
        final double xOffset = (dimensions.getWidth() - boardWidth) / 2;
        final double yOffset = (dimensions.getHeight() - boardHeight) / 2;
        boardImage.setX(xOffset);
        boardImage.setY(yOffset);

        // Creating the squares
        final ArrayList<Integer> repeatSquares = new ArrayList<>(Arrays.asList(5, 9, 13, 18, 24, 28, 34, 36, 40, 45, 49, 54));
        Square previousSquare = null;
        BridgeSquare beginBridge = null;
        for (int i = 0; i < 64; i++) {
            final JsonObject jsonPos = (JsonObject) positions.get(i);
            final Position position = new Position(xOffset + jsonPos.get("x").getAsDouble() * scaleRatio,
                yOffset + jsonPos.get("y").getAsDouble() * scaleRatio);

            final Square newSquare;
            if (repeatSquares.contains(i)) {
                newSquare = new RepeatSquare(i, position, previousSquare, this);
            } else if (i == 31 || i == 52) {
                newSquare = new PrisonSquare(i, position, previousSquare, this,
                    i == 31 ? "Player %d fell into a well" : "Player %d got locked up in prison");
            } else if (i == 19) {
                newSquare = new SkipSquare(i, position, previousSquare, this, 2);
            } else if (i == 58) {
                newSquare = new RestartSquare(i, position, previousSquare, this, firstSquare);
            } else if (i == 7) {
                beginBridge = new BridgeSquare(i, position, previousSquare, this);
                newSquare = beginBridge;
            } else if (i == 63) {
                newSquare = new EndSquare(i, position, previousSquare, this);
            } else {
                newSquare = new Square(i, position, previousSquare, this);
            }

            if (i == 32) {
                beginBridge.setDestinationSquare(newSquare);
            }

            if (previousSquare != null) {
                previousSquare.setNextSquare(newSquare);
            } else {
                firstSquare = newSquare;
            }
            previousSquare = newSquare;
        }

        // Creating the pawns
        pawns = new ArrayList<>();
        bibouleColors = new ArrayList<>(Arrays.asList("Blue", "Orange", "green", "Yellow", "Red"));
        for (int i = 0; i < nbPlayers; i++) {
            final ImageView imagePawn = new ImageView(String.format(BIBOULEPATH, bibouleColors.get(i)));
            imagePawn.setFitHeight(dimensions.getWidth() / 20);
            imagePawn.setFitWidth(dimensions.getWidth() / 20);
            pawns.add(new Pawn(imagePawn, firstSquare, i + 1, gameContext.getAnimationSpeedRatioSource()));
        }

        // Creating the turn indicator, it shows which biboule's turn it is
        turnIndicator = new ImageView(String.format(BIBOULEPATH, bibouleColors.get(0)));
        turnIndicator.setFitHeight(dimensions.getWidth() / 12);
        turnIndicator.setFitWidth(dimensions.getWidth() / 12);

        showPlayingBiboule = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(turnIndicator.layoutXProperty(),
                    (dimensions.getWidth() / 2) - turnIndicator.getFitWidth() / 2),
                new KeyValue(turnIndicator.layoutYProperty(),
                    (dimensions.getHeight() / 2) - turnIndicator.getFitHeight() / 2),
                new KeyValue(turnIndicator.scaleXProperty(), 2),
                new KeyValue(turnIndicator.scaleYProperty(), 2),
                new KeyValue(turnIndicator.opacityProperty(), 0)),
            new KeyFrame(Duration.seconds(2),
                new KeyValue(turnIndicator.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(turnIndicator.layoutXProperty(),
                    (dimensions.getWidth() / 2) - turnIndicator.getFitWidth() / 2),
                new KeyValue(turnIndicator.layoutYProperty(),
                    (dimensions.getHeight() / 2) - turnIndicator.getFitHeight() / 2),
                new KeyValue(turnIndicator.scaleXProperty(), 2),
                new KeyValue(turnIndicator.scaleYProperty(), 2)),
            new KeyFrame(Duration.seconds(3),
                new KeyValue(turnIndicator.layoutXProperty(),
                    11 * dimensions.getWidth() / 12 - dimensions.getWidth() / 25),
                new KeyValue(turnIndicator.layoutYProperty(), dimensions.getWidth() / 25),
                new KeyValue(turnIndicator.scaleXProperty(), 1),
                new KeyValue(turnIndicator.scaleYProperty(), 1)));

        showPlayingBiboule.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        showPlayingBiboule.setOnFinished(
            e -> rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollButton.getImage().getFitHeight()));

        // The dice are set in a grid pane, one next to the other
        diceDisplay = new GridPane();
        diceDisplay.setHgap(dimensions.getWidth() / 20);
        rolls = new int[2];
        diceRollers = new ArrayList<>();
        final float dieWidth = (float) (dimensions.getWidth() / 20);

        for (int i = 0; i < 2; i++) {
            final DiceRoller dr = new DiceRoller(dieWidth);
            diceRollers.add(dr);
            diceDisplay.add(dr, i, 0);
            // init rolls to 1s
            rolls[i] = 1;
        }

        // Animation to move the dice in and out of the center if the window
        moveDiceIn = new Timeline(new KeyFrame(Duration.seconds(1),
            new KeyValue(diceDisplay.layoutXProperty(), dimensions.getWidth() / 2 - 3 * dieWidth,
                Interpolator.EASE_OUT),
            new KeyValue(diceDisplay.layoutYProperty(), dimensions.getHeight() / 2 - dieWidth,
                Interpolator.EASE_OUT),
            new KeyValue(diceDisplay.scaleXProperty(), 1), new KeyValue(diceDisplay.scaleYProperty(), 1)));
        moveDiceOut = new Timeline(new KeyFrame(Duration.seconds(1),
            new KeyValue(diceDisplay.layoutXProperty(), -dieWidth, Interpolator.EASE_OUT),
            new KeyValue(diceDisplay.layoutYProperty(), 0, Interpolator.EASE_OUT),
            new KeyValue(diceDisplay.scaleXProperty(), 0.5), new KeyValue(diceDisplay.scaleYProperty(), 0.5)));

        moveDiceIn.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        moveDiceOut.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        // Dice are put in their default location, smaller, in the upper left corner
        diceDisplay.setScaleX(0.5);
        diceDisplay.setScaleY(0.5);
        diceDisplay.setLayoutX(-dieWidth);

        // Button which starts the beginning of a turn by rolling the dice
        rollButton = new ProgressButton();
        final ImageView rollImage = new ImageView("data/dice/roll.png");
        rollImage.setFitHeight(dimensions.getHeight() / 6);
        rollImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - rollImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollImage.getFitHeight());
        rollButton.setImage(rollImage);
        rollButton.assignIndicator(event -> roll(), config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        // The message queue
        messages = new VBox();
        messages.setAlignment(Pos.CENTER);
    }

    /***
     * Rolls the dice, and hides the roll button
     */
    private void roll() {
        rollButton.setLayoutY(dimensions.getHeight() * 2);
        moveDiceIn.setOnFinished(e -> {
            for (int i = 0; i < diceRollers.size(); i++) {
                rolls[i] = diceRollers.get(i).roll(i == 0 ? action -> playTurn() : null);
            }
        });
        moveDiceIn.play();
    }

    /***
     * Computes the dice result and sends it to the pawn in play.
     */
    private void playTurn() {
        moveDiceOut.play();
        final int rollResult = rolls[0] + rolls[1];
        pawns.get(currentPawn).move(rollResult);
    }

    /***
     * Called at launch, fills the gamecontext
     */
    @Override
    public void launch() {
        final Rectangle background = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        background.setFill(Color.GRAY);

        gameContext.getChildren().addAll(background, boardImage);

        for (final Pawn pawn : pawns) {
            pawn.reset(firstSquare);
            gameContext.getChildren().add(pawn.getPawnDisplay());
        }
        currentPawn = nbPlayers - 1;

        gameContext.getChildren().addAll(diceDisplay, rollButton, turnIndicator, messages);
        stats.notifyNewRoundReady();
        endOfTurn();
    }

    /***
     * Is called to reset the game for a new one, pawns are put at the beginning of the track.
     */
    @Override
    public void dispose() {
        gameContext.getChildren().remove(gameContext.getChildren().size() - 1);
        for (final Pawn pawn : pawns) {
            pawn.reset(firstSquare);
        }
        currentPawn = 0;
        turnIndicator.setImage(new Image(String.format(BIBOULEPATH, bibouleColors.get(currentPawn))));
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollButton.getImage().getFitHeight());
    }

    /***
     * Game can get stuck, if there are only 2 players, and they both are stuck.
     *
     * @return true if stuck
     */
    private boolean isGameStuck() {
        int i = 0;
        while (i < pawns.size() && pawns.get(i).isStuck()) {
            i++;
        }
        return i == pawns.size();
    }

    /***
     * Called at the end of a turn, switches to the next pawn in line, and checks if the game is not stuck
     */
    public void endOfTurn() {
        if (isGameStuck()) {
            showMessage("STUCK");
            launch();
        } else {
            Pawn pawn;
            do {
                currentPawn = (currentPawn + 1) % nbPlayers;
                pawn = pawns.get(currentPawn);
                if (pawn.isStuck()) {
                    showMessage("Player %d is stuck", pawn.getNumber());
                } else if (pawn.isSleeping()) {
                    showMessage("Player %d is asleep for %d more turn(s)", pawn.getNumber(), pawn.getTurnsLeftToSkip());
                }
            } while (pawn.isSleeping() || pawn.isStuck());

            turnIndicator.setImage(new Image(String.format(BIBOULEPATH, bibouleColors.get(currentPawn))));
            showPlayingBiboule.play();
            showMessage("Player %d's turn", pawn.getNumber());
        }
    }

    /***
     * Show a message on screen, the messages are in a vertical queue. The message is translated, not the values
     *
     */
    public void showMessage(final String message, final Object... values) {
        final Text messageText = new Text(0, dimensions.getHeight() / 3,
            String.format(gameContext.getTranslator().translate(message), values));
        messageText.setTextAlignment(TextAlignment.CENTER);
        messageText.setFill(Color.WHITE);
        messageText.setFont(new Font(dimensions.getHeight() / 10));
        messageText.setWrappingWidth(dimensions.getWidth());
        messageText.setOpacity(0);

        messages.getChildren().add(messageText);

        final Timeline showMessage = new Timeline(
            new KeyFrame(Duration.seconds(0.3), new KeyValue(messageText.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(4), new KeyValue(messageText.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(4.3), new KeyValue(messageText.opacityProperty(), 0)));

        showMessage.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        showMessage.setOnFinished(e -> messages.getChildren().remove(messageText));

        showMessage.play();
    }

    /***
     * Called when a pawn reaches square 63
     *
     * @param pawn
     *            winner pawn
     */
    void winner(final Pawn pawn) {
        showMessage("Player %d wins the game", pawn.getNumber());
        gameContext.playWinTransition(200, actionEvent -> dispose());
    }

    public void playMovementSound() {

        try {
            ForegroundSoundsUtils.playSound(String.format("data/goosegame/sounds/mvmt%d.wav", random.nextInt(6)));
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }
}
