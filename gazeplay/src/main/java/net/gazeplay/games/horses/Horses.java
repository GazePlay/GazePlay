package net.gazeplay.games.horses;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.DepthTest;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.DiceRoller;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
public class Horses implements GameLifeCycle {

    private static final String BIBOULEPATH = "data/horses/biboules/%s.png";
    private static final int NBPAWNS = 4;

    public enum TEAMS {
        BLUE, RED, GREEN, YELLOW
    }

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;
    @Getter
    private final Multilinguism translate;

    private final ImageView boardImage;
    private double gridElementSize;

    private final DiceRoller die;
    private final ProgressButton rollButton;
    private int diceOutcome;

    private ArrayList<ProgressButton> teamChoosers;
    private ArrayList<TEAMS> chosenTeams;
    private int currentTeam;
    private int nbTeamsChosen;
    private HashMap<TEAMS, Square> startSquares;
    private HashMap<TEAMS, ArrayList<Pawn>> pawns;
    private HashMap<TEAMS, ArrayList<Position>> spawnPoints;

    public Horses(GameContext gameContext, Stats stats, int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();
        this.translate = Multilinguism.getSingleton();

        boardImage = new ImageView("data/horses/horsiesboard.png");
        double imageSize = Math.min(dimensions.getHeight(), dimensions.getWidth());
        gridElementSize = imageSize / 15;
        double xOffset = (dimensions.getWidth() - imageSize) / 2;
        double yOffset = (dimensions.getHeight() - imageSize) / 2;
        boardImage.setFitHeight(imageSize);
        boardImage.setFitWidth(imageSize);
        boardImage.setX(xOffset);
        boardImage.setY(yOffset);
        gameContext.getChildren().add(boardImage);

        die = new DiceRoller((float) gridElementSize / 2);
        double diePositionInImage = (14 * imageSize) / 30;
        StackPane dieContainer = new StackPane();
        dieContainer.getChildren().add(die);
        dieContainer.setLayoutX(xOffset + diePositionInImage);
        dieContainer.setLayoutY(yOffset + diePositionInImage);
        gameContext.getChildren().add(dieContainer);

        rollButton = new ProgressButton();
        ImageView rollImage = new ImageView("data/dice/roll.png");
        rollImage.setFitHeight(dimensions.getHeight() / 6);
        rollImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - rollImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() / 2 - rollImage.getFitHeight() / 2);
        rollButton.setImage(rollImage);
        rollButton.assignIndicator(event -> roll(), config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        startSquares = new HashMap<>();
        pawns = new HashMap<>();
        spawnPoints = new HashMap<>();
        teamChoosers = new ArrayList<>();
        chosenTeams = new ArrayList<>();
        nbTeamsChosen = 0;

        HashMap<TEAMS, double[]> teamChooserPositions = new HashMap<>();
        teamChooserPositions.put(TEAMS.YELLOW, new double[] { xOffset, yOffset });
        teamChooserPositions.put(TEAMS.BLUE, new double[] { xOffset + 9 * gridElementSize, yOffset });
        teamChooserPositions.put(TEAMS.RED,
                new double[] { xOffset + 9 * gridElementSize, yOffset + 9 * gridElementSize });
        teamChooserPositions.put(TEAMS.GREEN, new double[] { xOffset, yOffset + 9 * gridElementSize });

        JsonParser parser = new JsonParser();
        JsonObject positions = null;
        try {
            positions = (JsonObject) parser.parse(new InputStreamReader(
                    Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("data/horses/positions.json")),
                    "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        double scaleRatio = imageSize / boardImage.getImage().getHeight();
        Square loopBack = null;
        Square previousCommonSquare = null;
        Square centerSquare = new FinishSquare(new Position(xOffset + imageSize / 2, yOffset + imageSize / 2), this);
        for (TEAMS team : TEAMS.values()) {
            JsonObject teamObject = (JsonObject) positions.get(team.name());
            JsonArray finalPath = (JsonArray) teamObject.get("finalPath");
            JsonArray commonPath = (JsonArray) teamObject.get("commonPath");
            JsonArray spawnPointsArray = (JsonArray) teamObject.get("spawnPoints");

            Square firstFromFinal = null;
            Square previousSquare = null;
            for (int i = 0; i < finalPath.size(); i++) {
                JsonObject object = (JsonObject) finalPath.get(i);
                Position position = new Position(xOffset + object.get("x").getAsDouble() * scaleRatio,
                        yOffset + object.get("y").getAsDouble() * scaleRatio);
                FinalPathSquare square = new FinalPathSquare(position, this, i + 1);
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
                JsonObject object = (JsonObject) commonPath.get(i);
                Position position = new Position(xOffset + object.get("x").getAsDouble() * scaleRatio,
                        yOffset + object.get("y").getAsDouble() * scaleRatio);
                Square square = null;
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
                if (team == TEAMS.YELLOW && i == commonPath.size() - 1) {
                    square.setNextSquare(loopBack);
                }
                previousCommonSquare = square;
            }

            ArrayList<Position> spawnPositionsList = new ArrayList<>();
            for (int i = 0; i < NBPAWNS; i++) {
                JsonObject object = (JsonObject) spawnPointsArray.get(i);
                Position position = new Position(
                        xOffset + object.get("x").getAsDouble() * scaleRatio - gridElementSize / 2,
                        yOffset + object.get("y").getAsDouble() * scaleRatio - gridElementSize / 2);
                spawnPositionsList.add(position);
            }
            spawnPoints.put(team, spawnPositionsList);

            ProgressButton chooseButton = new ProgressButton();
            chooseButton.setPrefWidth(6 * gridElementSize);
            chooseButton.setPrefHeight(6 * gridElementSize);
            chooseButton.setLayoutX(teamChooserPositions.get(team)[0]);
            chooseButton.setLayoutY(teamChooserPositions.get(team)[1]);
            chooseButton.assignIndicator(e -> {
                gameContext.getChildren().remove(chooseButton);
                selectTeam(team);
            }, config.getFixationLength());
            gameContext.getGazeDeviceManager().addEventFilter(chooseButton);
            chooseButton.active();
            gameContext.getChildren().add(chooseButton);
            teamChoosers.add(chooseButton);
        }
    }

    private void selectTeam(TEAMS team) {
        chosenTeams.add(team);
        ArrayList<Pawn> pawnList = new ArrayList();
        ArrayList<Position> spawnPositions = spawnPoints.get(team);
        for (int i = 0; i < NBPAWNS; i++) {
            StackPane pawnDisplay = new StackPane();
            pawnDisplay.setAlignment(Pos.CENTER);
            pawnDisplay.setLayoutX(spawnPositions.get(i).getX());
            pawnDisplay.setLayoutY(spawnPositions.get(i).getY());

            ImageView bibouleImage = new ImageView(String.format(BIBOULEPATH, team.toString()));
            bibouleImage.setFitHeight(gridElementSize);
            bibouleImage.setFitWidth(gridElementSize);

            ProgressButton button = new ProgressButton();
            ImageView selector = new ImageView("data/horses/selector.png");
            selector.setFitHeight(gridElementSize * 1.5);
            selector.setFitWidth(gridElementSize * 1.5);
            button.setImage(selector);
            button.disable();
            gameContext.getGazeDeviceManager().addEventFilter(button);

            pawnDisplay.getChildren().addAll(bibouleImage, button);
            gameContext.getChildren().add(pawnDisplay);

            Pawn pawn = new Pawn(team, pawnDisplay, button, spawnPositions.get(i), startSquares.get(team));
            pawnList.add(pawn);
        }
        pawns.put(team, pawnList);
        nbTeamsChosen++;
        if (nbTeamsChosen >= nbPlayers) {
            gameContext.getChildren().removeAll(teamChoosers);
            startGame();
        }
    }

    private void startGame() {
        gameContext.getChildren().add(rollButton);
        currentTeam = 0;
    }

    private void roll() {
        rollButton.disable();
        diceOutcome = die.roll(e -> showMovablePawns());
    }

    private void showMovablePawns() {
        ArrayList<Pawn> currentPawns = pawns.get(chosenTeams.get(currentTeam));
        int nbNonMovablePawns = 0;
        for (Pawn pawn : currentPawns) {
            if (!pawn.isOnTrack() /*&& diceOutcome == 6*/ && !startSquares.get(chosenTeams.get(currentTeam)).isOccupied()) {
                pawn.activate(e -> {
                    deactivatePawns();
                    pawn.spawn();
                }, config.getFixationLength(), gameContext);
            } else if (pawn.isOnTrack() && pawn.canMove(diceOutcome)) {
                pawn.activate(e -> {
                    deactivatePawns();
                    pawn.move(diceOutcome);
                }, config.getFixationLength(), gameContext);
            } else {
                nbNonMovablePawns++;
            }
        }
        if (nbNonMovablePawns == currentPawns.size()) {
            endOfTurn();
        }
    }

    private void deactivatePawns() {
        ArrayList<Pawn> currentPawns = pawns.get(chosenTeams.get(currentTeam));
        for (Pawn pawn : currentPawns) {
            pawn.deactivate(gameContext);
        }
    }

    public void endOfTurn() {
        rollButton.active();
        if (diceOutcome != 6) {
            currentTeam = (currentTeam + 1) % nbPlayers;
        }
    }

    public void win(Pawn pawn) {
        gameContext.playWinTransition(100, e -> {

        });
    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}
