package net.gazeplay.games.horses;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.DiceRoller;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressButton;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Game "Horses" (French "petits chevaux"; reviewed on 30/10/2026, REVUE_DES_JEUX.md), on a normal or a simplified
 * board, for 2 to 4 teams, played by people taking turns or by the computer (see {@link HorsesVariant}). The people
 * choose their team by a fixation on its stable; each team in turn rolls the die (a fixation on the button in the middle) and
 * chooses a pawn to move (a fixation on a pawn with a pulsing ring). A 6 takes a pawn out of its stable and gives
 * another turn; a pawn landing on a pawn of another team sends it back to its stable; a pawn must climb its final path
 * with the exact numbers; the first pawn arriving wins.
 * <p>
 * Statistics: a goal to reach for each turn of a person, reached when the die is rolled.
 */
@Slf4j
public class Horses implements GameLifeCycle {

    private static final String BIBOULEPATH = "data/horses/biboules/%s.png";
    private static final String ROLLIMAGESPATH = "data/horses/rollImages/roll%s.png";

    /**
     * Pause before each step of the computer (rolling, choosing a pawn), so that the players follow it.
     */
    private static final Duration COMPUTER_PAUSE = Duration.seconds(1.2);

    /**
     * The teams, each with its color.
     */
    public enum TEAMS {
        BLUE, RED, GREEN, YELLOW
    }

    /**
     * Colors of the teams, as on the board (gradle/images/make_horses_images.py).
     */
    private static final Map<TEAMS, Color> TEAM_COLORS = new EnumMap<>(Map.of(
        TEAMS.BLUE, Color.rgb(61, 155, 233), TEAMS.RED, Color.rgb(230, 88, 80),
        TEAMS.GREEN, Color.rgb(92, 184, 96), TEAMS.YELLOW, Color.rgb(246, 190, 46)));

    private final IGameContext gameContext;
    private final Stats stats;
    private final HorsesVariant variant;

    private Dimension2D dimensions;
    private Translator translator;
    private int nbPawns;

    private Group backgroundLayer;
    private Group foregroundLayer;
    private Group panelLayer;

    private double gridElementSize;
    private double xOffset;

    private DiceRoller die;
    private ProgressButton rollButton;
    private Map<TEAMS, ImageView> rollImages;
    private int diceOutcome;
    private final StackPane diceContainer;

    private List<ProgressButton> teamChoosers;
    @Getter
    private List<TEAMS> chosenTeams;
    @Getter
    private Set<TEAMS> computerTeams;
    private int currentTeam;
    private Map<TEAMS, Square> startSquares;
    @Getter
    private Map<TEAMS, List<Pawn>> pawns;
    private Map<TEAMS, List<Position>> spawnPoints;
    private Map<TEAMS, double[]> stables;
    private Circle stableHighlight;
    private Group turnPanel;
    private Group messageCard;
    private ImageView computerDie;

    /**
     * Pauses of the computer, stopped at the end of the game.
     */
    private final List<Animation> pauses = new ArrayList<>();

    @Getter
    private boolean over;

    private final ReplayablePseudoRandom randomGenerator;

    public Horses(final IGameContext gameContext, final Stats stats, final HorsesVariant variant) {
        this(gameContext, stats, variant, new ReplayablePseudoRandom());
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public Horses(final IGameContext gameContext, final Stats stats, final HorsesVariant variant,
                  final double gameSeed) {
        this(gameContext, stats, variant, new ReplayablePseudoRandom(gameSeed));
    }

    private Horses(final IGameContext gameContext, final Stats stats, final HorsesVariant variant,
                   final ReplayablePseudoRandom randomGenerator) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
        this.randomGenerator = randomGenerator;
        diceContainer = new StackPane();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Teams

    /**
     * A person chose a team (by gazing at its stable): its pawns are put in it. When all the people have chosen, the
     * computer takes the other teams, and the game starts.
     */
    void selectTeam(final TEAMS team) {
        addTeam(team);
        final int humansLeft = variant.getHumans() - chosenTeams.size();
        if (humansLeft > 0) {
            showMessage(translator.translate("HorsesTeamsLeft") + " " + humansLeft);
            return;
        }
        foregroundLayer.getChildren().removeAll(teamChoosers);
        for (final TEAMS other : TEAMS.values()) {
            if (chosenTeams.size() < variant.getTeams() && !chosenTeams.contains(other)) {
                computerTeams.add(other);
                addTeam(other);
            }
        }
        startGame();
    }

    private void addTeam(final TEAMS team) {
        chosenTeams.add(team);
        final List<Pawn> pawnList = new ArrayList<>();
        final List<Position> spawnPositions = spawnPoints.get(team);
        for (int i = 0; i < nbPawns; i++) {
            final ImageView bibouleImage = new ImageView(String.format(BIBOULEPATH, team.toString()));
            bibouleImage.setFitHeight(gridElementSize);
            bibouleImage.setFitWidth(gridElementSize);
            bibouleImage.setLayoutX(spawnPositions.get(i).getX());
            bibouleImage.setLayoutY(spawnPositions.get(i).getY());

            final Circle ring = new Circle(gridElementSize * 0.6, Color.TRANSPARENT);
            ring.setStroke(TEAM_COLORS.get(team).darker());
            ring.setStrokeWidth(gridElementSize * 0.12);
            ring.setMouseTransparent(true);

            // A transparent round button over the pawn, chosen by a fixation
            final ProgressButton button = new ProgressButton();
            button.getButton().setRadius(gridElementSize / 2);
            button.getButton().setFill(Color.TRANSPARENT);
            button.setLayoutX(spawnPositions.get(i).getX());
            button.setLayoutY(spawnPositions.get(i).getY());
            gameContext.getGazeDeviceManager().addEventFilter(button);

            backgroundLayer.getChildren().addAll(ring, bibouleImage);
            foregroundLayer.getChildren().add(button);

            pawnList.add(new Pawn(team, bibouleImage, button, ring, spawnPositions.get(i), startSquares.get(team),
                gameContext));
        }
        pawns.put(team, pawnList);
    }

    private TEAMS current() {
        return chosenTeams.get(currentTeam);
    }

    private boolean computerPlays() {
        return computerTeams.contains(current());
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Turns

    private void startGame() {
        currentTeam = -1;
        endOfTurn();
    }

    /**
     * Called at the end of a turn: the next team plays, or the same one again after a 6. A person rolls the die with
     * its button; the computer rolls it after a pause.
     */
    public void endOfTurn() {
        if (over) {
            return;
        }
        if (diceOutcome != 6) {
            currentTeam = (currentTeam + 1) % chosenTeams.size();
        }
        updateTurnPanel();
        foregroundLayer.getChildren().removeAll(diceContainer, rollButton, computerDie);
        if (computerPlays()) {
            computerDie.setImage(rollImages.get(current()).getImage());
            foregroundLayer.getChildren().add(computerDie);
            showMessage(translator.translate(diceOutcome == 6 ? "HorsesPlayAgain" : "HorsesComputerTurn"));
            later(this::roll);
        } else {
            stats.incrementNumberOfGoalsToReach();
            rollButton.setImage(rollImages.get(current()));
            rollButton.active();
            foregroundLayer.getChildren().add(rollButton);
            showMessage(translator.translate(diceOutcome == 6 ? "HorsesPlayAgain" : "HorsesRoll"));
        }
    }

    /**
     * Rolls the die (the button of the die is replaced by the die).
     */
    private void roll() {
        if (over) {
            return;
        }
        foregroundLayer.getChildren().removeAll(rollButton, computerDie);
        rollButton.disable();
        foregroundLayer.getChildren().add(diceContainer);
        diceOutcome = die.roll(e -> showMovablePawns());
    }

    /**
     * After a roll: the pawns of the team that can move. A person chooses one of them (they have a pulsing ring); the
     * computer chooses after a pause; with none, the turn ends.
     */
    private void showMovablePawns() {
        if (over) {
            return;
        }
        final List<Pawn> movable = new ArrayList<>();
        for (final Pawn pawn : pawns.get(current())) {
            if (pawn.canSpawn(diceOutcome) || pawn.canMove(diceOutcome)) {
                movable.add(pawn);
            }
        }
        if (movable.isEmpty()) {
            showMessage(translator.translate("HorsesNoMove"));
            later(this::endOfTurn);
        } else if (computerPlays()) {
            final Pawn chosen = computerChoice(movable, diceOutcome);
            chosen.showRing();
            later(() -> play(chosen));
        } else {
            showMessage(translator.translate("HorsesChoosePawn"));
            for (final Pawn pawn : movable) {
                pawn.activate(() -> play(pawn));
            }
        }
    }

    /**
     * Moves the chosen pawn: out of its stable on a 6, or along the track.
     */
    private void play(final Pawn pawn) {
        for (final Pawn other : pawns.get(current())) {
            other.deactivate();
        }
        if (pawn.isOnTrack()) {
            pawn.move(diceOutcome);
        } else {
            pawn.spawn();
        }
    }

    /**
     * @return the pawn the computer moves: one sending a pawn of another team back to its stable, else one on its
     *   final path, else one leaving its stable, else the one which went the furthest
     */
    Pawn computerChoice(final List<Pawn> movable, final int dice) {
        return movable.stream().max(Comparator
            .comparingInt((Pawn pawn) -> pawn.wouldCapture(dice) ? 1 : 0)
            .thenComparingInt(pawn -> pawn.isOnFinalPath() ? 1 : 0)
            .thenComparingInt(pawn -> pawn.isOnTrack() ? 0 : 1)
            .thenComparingInt(Pawn::getProgress)).orElseThrow();
    }

    /**
     * The first pawn arriving wins: "Bravo" when a person wins, then the statistics.
     */
    public void win(final Pawn pawn) {
        over = true;
        pauses.forEach(Animation::stop);
        final boolean byComputer = computerTeams.contains(pawn.getTeam());
        showMessage(translator.translate(byComputer ? "HorsesComputerWins" : "HorsesWins") + "\n"
            + translator.translate("HorsesTeam_" + pawn.getTeam()));
        if (byComputer) {
            final PauseTransition end = new PauseTransition(Duration.seconds(3.5));
            end.setOnFinished(e -> {
                dispose();
                gameContext.showRoundStats(stats, this);
            });
            end.play();
        } else {
            gameContext.playWinTransition(100, e -> {
                dispose();
                gameContext.showRoundStats(stats, this);
            });
        }
    }

    private void later(final Runnable action) {
        final PauseTransition pause = new PauseTransition(COMPUTER_PAUSE);
        pause.setOnFinished(e -> {
            pauses.remove(pause);
            if (!over) {
                action.run();
            }
        });
        pauses.add(pause);
        pause.play();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Panels

    /**
     * On the left of the board: the Biboule of the team whose turn it is, and its name (and "computer"); its stable is
     * circled.
     */
    private void updateTurnPanel() {
        panelLayer.getChildren().remove(turnPanel);
        final TEAMS team = current();
        final double width = Math.max(xOffset, dimensions.getWidth() * 0.18) * 0.9;
        final double fontSize = dimensions.getHeight() * 0.035;
        String text = translator.translate("HorsesTurn") + "\n" + translator.translate("HorsesTeam_" + team);
        if (computerPlays()) {
            text += "\n" + translator.translate("HorsesComputer");
        }
        final Group card = GameStyle.card(text, fontSize, width);
        final ImageView biboule = new ImageView(String.format(BIBOULEPATH, team));
        biboule.setPreserveRatio(true);
        biboule.setFitWidth(width * 0.6);
        biboule.setLayoutX((width - width * 0.6) / 2);
        card.setLayoutY(biboule.getBoundsInLocal().getHeight() + fontSize * 0.5);
        card.setLayoutX((width - card.getBoundsInLocal().getWidth()) / 2);
        turnPanel = new Group(biboule, card);
        turnPanel.setLayoutX(Math.max(0, (xOffset - width) / 2));
        turnPanel.setLayoutY(dimensions.getHeight() * 0.12);
        turnPanel.setMouseTransparent(true);
        panelLayer.getChildren().add(turnPanel);

        final double[] stable = stables.get(team);
        stableHighlight.setCenterX(stable[0]);
        stableHighlight.setCenterY(stable[1]);
        stableHighlight.setRadius(stable[2]);
        stableHighlight.setStroke(TEAM_COLORS.get(team).darker());
        stableHighlight.setVisible(true);
    }

    /**
     * Shows a message on a card at the right of the board (it replaces the previous one).
     */
    public void showMessage(final String message) {
        panelLayer.getChildren().remove(messageCard);
        final double width = Math.max(xOffset, dimensions.getWidth() * 0.18) * 0.9;
        messageCard = GameStyle.card(message, dimensions.getHeight() * 0.035, width);
        messageCard.setLayoutX(dimensions.getWidth() - Math.max(0, (xOffset - width) / 2)
            - messageCard.getBoundsInLocal().getWidth());
        messageCard.setLayoutY(dimensions.getHeight() * 0.4);
        messageCard.setMouseTransparent(true);
        panelLayer.getChildren().add(messageCard);
    }

    /**
     * @return the text of the message shown, null if none (for the tests)
     */
    String shownMessage() {
        if (messageCard == null) {
            return null;
        }
        return messageCard.getChildren().stream().filter(javafx.scene.text.Text.class::isInstance)
            .map(node -> ((javafx.scene.text.Text) node).getText()).findFirst().orElse(null);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Board

    @Override
    public void launch() {
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.translator = gameContext.getTranslator();
        diceOutcome = 0;
        over = false;
        pauses.clear();

        this.backgroundLayer = new Group();
        this.foregroundLayer = new Group();
        this.panelLayer = new Group();
        // The background of the menus (it used to be black)
        final Rectangle background = new Rectangle(dimensions.getWidth(), dimensions.getHeight());
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);
        backgroundLayer.getChildren().add(background);
        this.gameContext.getChildren().addAll(backgroundLayer, foregroundLayer, panelLayer);

        final String jsonPath = variant.isSimplified() ? "data/horses/positionsSimplified.json"
            : "data/horses/positions.json";
        final JsonObject positions = (JsonObject) JsonParser.parseReader(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(jsonPath)), StandardCharsets.UTF_8));

        nbPawns = positions.get("nbPawns").getAsInt();
        final int nbElementsPerSide = positions.get("elementsPerSide").getAsInt();
        final ImageView boardImage = new ImageView("data/horses/" + positions.get("imageName").getAsString());
        final double imageSize = Math.min(dimensions.getHeight(), dimensions.getWidth());
        gridElementSize = imageSize / nbElementsPerSide;
        xOffset = (dimensions.getWidth() - imageSize) / 2;
        final double yOffset = (dimensions.getHeight() - imageSize) / 2;
        boardImage.setFitHeight(imageSize);
        boardImage.setFitWidth(imageSize);
        boardImage.setX(xOffset);
        boardImage.setY(yOffset);
        backgroundLayer.getChildren().add(boardImage);

        stableHighlight = new Circle();
        stableHighlight.setFill(Color.TRANSPARENT);
        stableHighlight.setStrokeWidth(gridElementSize * 0.18);
        stableHighlight.setMouseTransparent(true);
        stableHighlight.setVisible(false);
        backgroundLayer.getChildren().add(stableHighlight);

        die = new DiceRoller((float) gridElementSize / 2, gameContext.getSoundManager(), randomGenerator);
        diceContainer.getChildren().setAll(die);
        final double diePositionInImage = imageSize / 2 - gridElementSize / 2;
        diceContainer.setLayoutX(xOffset + diePositionInImage);
        diceContainer.setLayoutY(yOffset + diePositionInImage);

        rollButton = new ProgressButton();
        rollButton.assignIndicatorUpdatable(event -> {
            stats.incrementNumberOfGoalsReached();
            roll();
        }, gameContext);
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        final double rollSize = dimensions.getHeight() / 6;
        rollButton.getButton().setRadius(rollSize / 2);
        rollButton.getButton().setFill(Color.TRANSPARENT);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - rollSize / 2);
        rollButton.setLayoutY(dimensions.getHeight() / 2 - rollSize / 2);
        computerDie = new ImageView();
        computerDie.setFitWidth(rollSize);
        computerDie.setFitHeight(rollSize);
        computerDie.setLayoutX(dimensions.getWidth() / 2 - rollSize / 2);
        computerDie.setLayoutY(dimensions.getHeight() / 2 - rollSize / 2);
        computerDie.setMouseTransparent(true);

        rollImages = new EnumMap<>(TEAMS.class);
        startSquares = new EnumMap<>(TEAMS.class);
        pawns = new EnumMap<>(TEAMS.class);
        spawnPoints = new EnumMap<>(TEAMS.class);
        stables = new EnumMap<>(TEAMS.class);
        teamChoosers = new ArrayList<>();
        chosenTeams = new ArrayList<>();
        computerTeams = new HashSet<>();

        final Map<TEAMS, double[]> teamChooserPositions = new EnumMap<>(TEAMS.class);
        final int elementOffset = (nbElementsPerSide - 3) / 2 + 3;
        teamChooserPositions.put(TEAMS.YELLOW, new double[]{xOffset, yOffset});
        teamChooserPositions.put(TEAMS.BLUE, new double[]{xOffset + elementOffset * gridElementSize, yOffset});
        teamChooserPositions.put(TEAMS.RED,
            new double[]{xOffset + elementOffset * gridElementSize, yOffset + elementOffset * gridElementSize});
        teamChooserPositions.put(TEAMS.GREEN, new double[]{xOffset, yOffset + elementOffset * gridElementSize});
        final double stableSize = (nbElementsPerSide - 3d) / 2d * gridElementSize;

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

            final List<Position> spawnPositionsList = new ArrayList<>();
            for (int i = 0; i < nbPawns; i++) {
                final JsonObject object = (JsonObject) spawnPointsArray.get(i);
                final Position position = new Position(
                    xOffset + object.get("x").getAsDouble() * scaleRatio - gridElementSize / 2,
                    yOffset + object.get("y").getAsDouble() * scaleRatio - gridElementSize / 2);
                spawnPositionsList.add(position);
            }
            spawnPoints.put(team, spawnPositionsList);

            final double[] corner = teamChooserPositions.get(team);
            stables.put(team, new double[]{corner[0] + stableSize / 2, corner[1] + stableSize / 2, stableSize * 0.48});

            final ProgressButton chooseButton = new ProgressButton();
            chooseButton.setPrefWidth(stableSize);
            chooseButton.setPrefHeight(stableSize);
            chooseButton.setLayoutX(corner[0]);
            chooseButton.setLayoutY(corner[1]);
            chooseButton.assignIndicatorUpdatable(e -> {
                foregroundLayer.getChildren().remove(chooseButton);
                selectTeam(team);
            }, gameContext);
            gameContext.getGazeDeviceManager().addEventFilter(chooseButton);
            chooseButton.active();
            foregroundLayer.getChildren().add(chooseButton);
            teamChoosers.add(chooseButton);

            final ImageView rollImage = new ImageView(String.format(ROLLIMAGESPATH, team));
            rollImage.setFitHeight(rollSize);
            rollImage.setFitWidth(rollSize);
            rollImages.put(team, rollImage);
        }
        loopBack.setPreviousSquare(previousCommonSquare);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        showMessage(translator.translate(variant.getHumans() == 1 ? "HorsesChooseTeam" : "HorsesChooseTeams"));
    }

    @Override
    public void dispose() {
        over = true;
        pauses.forEach(Animation::stop);
        pauses.clear();
        this.gameContext.getChildren().clear();
        this.gameContext.clear();
    }
}
