package net.gazeplay.games.horses;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.DiceRoller;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressButton;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
public class Horses implements GameLifeCycle {

    private static final String BIBOULEPATH = "data/horses/biboules/%s.png";
    private static final String ROLLIMAGESPATH = "data/horses/rollImages/roll%s.png";

    public enum TEAMS {
        BLUE, RED, GREEN, YELLOW
    }

    private final IGameContext gameContext;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;
    @Getter
    private final Multilinguism translate;
    private final int nbPawns;

    private final Group backgroundLayer;
    private final Group foregroundLayer;

    private final double gridElementSize;

    private final DiceRoller die;
    private final ProgressButton rollButton;
    private final HashMap<TEAMS, ImageView> rollImages;
    private int diceOutcome;

    private final ArrayList<ProgressButton> teamChoosers;
    private final ArrayList<TEAMS> chosenTeams;
    private int currentTeam;
    private int nbTeamsChosen;
    private final HashMap<TEAMS, Square> startSquares;
    private final HashMap<TEAMS, ArrayList<Pawn>> pawns;
    private final HashMap<TEAMS, ArrayList<Position>> spawnPoints;
    private final HashMap<TEAMS, Color> fontColors;

    private final VBox messages;

    public Horses(final IGameContext gameContext, final Stats stats, final int gameVersion, final int nbPlayers) {
        this.gameContext = gameContext;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = gameContext.getConfiguration();
        this.translate = Multilinguism.getSingleton();

        this.backgroundLayer = new Group();
        this.foregroundLayer = new Group();
        messages = new VBox();
        messages.setAlignment(Pos.CENTER);
        this.gameContext.getChildren().addAll(backgroundLayer, messages, foregroundLayer);

        final String jsonPath;
        if (gameVersion == 0) {
            jsonPath = "data/horses/positions.json";
        } else {
            jsonPath = "data/horses/positionsSimplified.json";
        }

        final JsonParser parser = new JsonParser();
        final JsonObject positions;
        positions = (JsonObject) parser.parse(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(jsonPath)), StandardCharsets.UTF_8));

        nbPawns = positions.get("nbPawns").getAsInt();
        final int nbElementsPerSide = positions.get("elementsPerSide").getAsInt();
        final ImageView boardImage = new ImageView("data/horses/" + positions.get("imageName").getAsString());
        final double imageSize = Math.min(dimensions.getHeight(), dimensions.getWidth());
        gridElementSize = imageSize / nbElementsPerSide;
        final double xOffset = (dimensions.getWidth() - imageSize) / 2;
        final double yOffset = (dimensions.getHeight() - imageSize) / 2;
        boardImage.setFitHeight(imageSize);
        boardImage.setFitWidth(imageSize);
        boardImage.setX(xOffset);
        boardImage.setY(yOffset);
        backgroundLayer.getChildren().add(boardImage);

        die = new DiceRoller((float) gridElementSize / 2);
        final double diePositionInImage = imageSize / 2 - gridElementSize / 2;
        final StackPane dieContainer = new StackPane();
        dieContainer.getChildren().add(die);
        dieContainer.setLayoutX(xOffset + diePositionInImage);
        dieContainer.setLayoutY(yOffset + diePositionInImage);
        backgroundLayer.getChildren().add(dieContainer);

        rollButton = new ProgressButton();
        rollButton.assignIndicator(event -> roll(), config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        rollImages = new HashMap<>();
        startSquares = new HashMap<>();
        pawns = new HashMap<>();
        spawnPoints = new HashMap<>();
        teamChoosers = new ArrayList<>();
        chosenTeams = new ArrayList<>();
        fontColors = new HashMap<>();
        nbTeamsChosen = 0;

        fontColors.put(TEAMS.BLUE, Color.LIGHTBLUE);
        fontColors.put(TEAMS.YELLOW, Color.GOLD);
        fontColors.put(TEAMS.RED, Color.INDIANRED);
        fontColors.put(TEAMS.GREEN, Color.LIGHTGREEN);

        final HashMap<TEAMS, double[]> teamChooserPositions = new HashMap<>();
        final int elementOffset = (nbElementsPerSide - 3) / 2 + 3;
        teamChooserPositions.put(TEAMS.YELLOW, new double[]{xOffset, yOffset});
        teamChooserPositions.put(TEAMS.BLUE, new double[]{xOffset + elementOffset * gridElementSize, yOffset});
        teamChooserPositions.put(TEAMS.RED,
            new double[]{xOffset + elementOffset * gridElementSize, yOffset + elementOffset * gridElementSize});
        teamChooserPositions.put(TEAMS.GREEN, new double[]{xOffset, yOffset + elementOffset * gridElementSize});

        final double scaleRatio = imageSize / boardImage.getImage().getHeight();
        Square loopBack = null;
        Square previousCommonSquare = null;
        final Square centerSquare = new FinishSquare(new Position(xOffset + imageSize / 2, yOffset + imageSize / 2), this);
        for (final TEAMS team : TEAMS.values()) {
            final JsonObject teamObject = (JsonObject) positions.get(team.name());
            final JsonArray finalPath = (JsonArray) teamObject.get("finalPath");
            final JsonArray commonPath = (JsonArray) teamObject.get("commonPath");
            final JsonArray spawnPointsArray = (JsonArray) teamObject.get("spawnPoints");

            Square firstFromFinal = null;
            Square previousSquare = null;
            for (int i = 0; i < finalPath.size(); i++) {
                final JsonObject object = (JsonObject) finalPath.get(i);
                final Position position = new Position(xOffset + object.get("x").getAsDouble() * scaleRatio,
                    yOffset + object.get("y").getAsDouble() * scaleRatio);
                final FinalPathSquare square = new FinalPathSquare(position, this, i + 1);
                square.setPreviousSquare(previousSquare);
                if (previousSquare != null) {
                    previousSquare.setNextSquare(square);
                }
                if (i == 0) {
                    firstFromFinal = square;
                }
                previousSquare = square;
            }
            previousSquare.setNextSquare(centerSquare);

            for (int i = 0; i < commonPath.size(); i++) {
                final JsonObject object = (JsonObject) commonPath.get(i);
                final Position position = new Position(xOffset + object.get("x").getAsDouble() * scaleRatio,
                    yOffset + object.get("y").getAsDouble() * scaleRatio);
                final Square square;
                if (i == 0) {
                    square = new FinalPathStart(position, this, team, firstFromFinal);
                    firstFromFinal.setPreviousSquare(square);
                } else if (i == 1) {
                    square = new StartSquare(position, this, team);
                } else {
                    square = new Square(position, this);
                }

                if (previousCommonSquare == null) {
                    loopBack = square;
                } else {
                    previousCommonSquare.setNextSquare(square);
                    square.setPreviousSquare(previousCommonSquare);
                }

                if (i == 1) {
                    startSquares.put(team, square);
                }
                if (team == TEAMS.values()[TEAMS.values().length - 1] && i == commonPath.size() - 1) {
                    square.setNextSquare(loopBack);
                }
                previousCommonSquare = square;
            }

            final ArrayList<Position> spawnPositionsList = new ArrayList<>();
            for (int i = 0; i < nbPawns; i++) {
                final JsonObject object = (JsonObject) spawnPointsArray.get(i);
                final Position position = new Position(
                    xOffset + object.get("x").getAsDouble() * scaleRatio - gridElementSize / 2,
                    yOffset + object.get("y").getAsDouble() * scaleRatio - gridElementSize / 2);
                spawnPositionsList.add(position);
            }
            spawnPoints.put(team, spawnPositionsList);

            final ProgressButton chooseButton = new ProgressButton();
            chooseButton.setPrefWidth((nbElementsPerSide - 3d) / 2d * gridElementSize);
            chooseButton.setPrefHeight((nbElementsPerSide - 3d) / 2d * gridElementSize);
            chooseButton.setLayoutX(teamChooserPositions.get(team)[0]);
            chooseButton.setLayoutY(teamChooserPositions.get(team)[1]);
            chooseButton.assignIndicator(e -> {
                foregroundLayer.getChildren().remove(chooseButton);
                selectTeam(team);
            }, config.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(chooseButton);
            chooseButton.active();
            foregroundLayer.getChildren().add(chooseButton);
            teamChoosers.add(chooseButton);

            final ImageView rollImage = new ImageView(String.format(ROLLIMAGESPATH, team));
            rollImage.setFitHeight(dimensions.getHeight() / 6);
            rollImage.setFitWidth(dimensions.getHeight() / 6);
            rollImages.put(team, rollImage);
        }
        loopBack.setPreviousSquare(previousCommonSquare);
    }

    /**
     * When a team is selected (by gazing at a big round button) This adds the pawns to the game, in their initial
     * position
     */
    private void selectTeam(final TEAMS team) {
        chosenTeams.add(team);
        final ArrayList<Pawn> pawnList = new ArrayList();
        final ArrayList<Position> spawnPositions = spawnPoints.get(team);
        for (int i = 0; i < nbPawns; i++) {

            final ImageView bibouleImage = new ImageView(String.format(BIBOULEPATH, team.toString()));
            bibouleImage.setFitHeight(gridElementSize);
            bibouleImage.setFitWidth(gridElementSize);
            bibouleImage.setLayoutX(spawnPositions.get(i).getX());
            bibouleImage.setLayoutY(spawnPositions.get(i).getY());

            final ProgressButton button = new ProgressButton();
            final ImageView selector = new ImageView("data/horses/selector.png");
            selector.setFitHeight(gridElementSize);
            selector.setFitWidth(gridElementSize);
            button.setImage(selector);
            button.setLayoutX(spawnPositions.get(i).getX());
            button.setLayoutY(spawnPositions.get(i).getY());
            button.disable();
            gameContext.getGazeDeviceManager().addEventFilter(button);

            backgroundLayer.getChildren().add(bibouleImage);
            foregroundLayer.getChildren().add(button);

            final Pawn pawn = new Pawn(team, bibouleImage, button, spawnPositions.get(i), startSquares.get(team));
            pawnList.add(pawn);
        }
        pawns.put(team, pawnList);
        nbTeamsChosen++;
        if (nbTeamsChosen >= nbPlayers) {
            foregroundLayer.getChildren().removeAll(teamChoosers);
            startGame();
        } else {
            showMessage(Color.WHITE, "%d more team(s) to select", nbPlayers - nbTeamsChosen);
        }
    }

    /**
     * Is called when teams have been selected
     */
    private void startGame() {
        foregroundLayer.getChildren().add(rollButton);
        currentTeam = -1;
        endOfTurn();
    }

    /**
     * Hides the roll button out of the way, and rolls the die
     */
    private void roll() {
        rollButton.setLayoutX(-1000);
        rollButton.setLayoutY(-1000);
        diceOutcome = die.roll(e -> showMovablePawns());
    }

    /**
     * Checks which pawns from the current team are allowed to move, if they are, their button is activated
     */
    private void showMovablePawns() {
        final ArrayList<Pawn> currentPawns = pawns.get(chosenTeams.get(currentTeam));
        int nbNonMovablePawns = 0;
        for (final Pawn pawn : currentPawns) {
            if (!pawn.isOnTrack() && diceOutcome == 6 && !startSquares.get(chosenTeams.get(currentTeam)).isOccupied()) {
                pawn.activate(e -> {
                    deactivatePawns();
                    pawn.spawn();
                }, config.getFixationLength());
            } else if (pawn.isOnTrack() && pawn.canMove(diceOutcome)) {
                pawn.activate(e -> {
                    deactivatePawns();
                    pawn.move(diceOutcome);
                }, config.getFixationLength());
            } else {
                nbNonMovablePawns++;
            }
        }
        if (nbNonMovablePawns == currentPawns.size()) {
            showMessage(getCurrentFontColor(), "No moves available");
            endOfTurn();
        } else {
            showMessage(getCurrentFontColor(), "Select the pawn you want to move");
        }
    }

    /**
     * When a pawn is selected, all other pawns need to be deactivated
     */
    private void deactivatePawns() {
        final ArrayList<Pawn> currentPawns = pawns.get(chosenTeams.get(currentTeam));
        for (final Pawn pawn : currentPawns) {
            pawn.deactivate();
        }
    }

    /**
     * @return the color associated to the current team playing
     */
    private Color getCurrentFontColor() {
        return fontColors.get(chosenTeams.get(currentTeam));
    }

    /**
     * Displays a message in a vertical queue, which disappears after a short time
     */
    public void showMessage(final Color fontColor, final String message, final Object... values) {
        final Text messageText = new Text(0, dimensions.getHeight() / 3,
            String.format(translate.getTrad(message, config.getLanguage()), values));
        messageText.setTextAlignment(TextAlignment.CENTER);
        messageText.setFill(fontColor);
        messageText.setFont(new Font(dimensions.getHeight() / 10));
        messageText.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        messageText.setWrappingWidth(dimensions.getWidth());
        messageText.setOpacity(0);

        messages.getChildren().add(messageText);

        final Timeline showMessage = new Timeline(
            new KeyFrame(Duration.seconds(0.3), new KeyValue(messageText.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(4), new KeyValue(messageText.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(4.3), new KeyValue(messageText.opacityProperty(), 0)));

        showMessage.setOnFinished(e -> messages.getChildren().remove(messageText));

        showMessage.playFromStart();
    }

    /**
     * Called at the end of a turn, it gives the turn to the next team, or leaves it to the current if a 6 was rolled It
     * resets the roll button, with the appropriate color
     */
    public void endOfTurn() {
        if (diceOutcome != 6) {
            currentTeam = (currentTeam + 1) % nbPlayers;
            showMessage(getCurrentFontColor(), "%s team's turn",
                translate.getTrad(chosenTeams.get(currentTeam).toString().toLowerCase(), config.getLanguage()));
        } else {
            showMessage(getCurrentFontColor(), "Play again");
        }
        final ImageView rollImage = rollImages.get(chosenTeams.get(currentTeam));
        rollButton.setLayoutX(dimensions.getWidth() / 2 - rollImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() / 2 - rollImage.getFitHeight() / 2);
        rollButton.setImage(rollImage);
    }

    /**
     * Plays the win animation at the end of the game
     */
    public void win(final Pawn pawn) {
        showMessage(getCurrentFontColor(), "%s team wins",
            translate.getTrad(chosenTeams.get(currentTeam).toString().toLowerCase(), config.getLanguage()));
        gameContext.playWinTransition(100, e -> {

        });
    }

    @Override
    public void launch() {
        showMessage(Color.WHITE, "Select teams by looking at the big circles");
    }

    @Override
    public void dispose() {

    }
}
