package net.gazeplay.games.horses;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.scene.DepthTest;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import lombok.Data;
import lombok.Getter;
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

public class Horses implements GameLifeCycle {

    private static final String BIBOULEPATH = "data/biboulejump/biboules/%s.png";
    public enum TEAMS {BLUE, RED, GREEN, YELLOW}

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;
    @Getter
    private final Multilinguism translate;

    private final ImageView boardImage;
    private final DiceRoller die;
    private final ProgressButton rollButton;
    private int diceOutcome;

    private HashMap<TEAMS, Square> startSquares;
    private HashMap<TEAMS, ArrayList<Pawn>> pawns;

    public Horses(GameContext gameContext, Stats stats, int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();
        this.translate = Multilinguism.getSingleton();

        boardImage = new ImageView("data/horses/horsiesboard.png");
        double imageSize = Math.min(dimensions.getHeight(), dimensions.getWidth());
        double xOffset = (dimensions.getWidth() - imageSize)/2;
        double yOffset = (dimensions.getHeight() - imageSize)/2;
        boardImage.setFitHeight(imageSize);
        boardImage.setFitWidth(imageSize);
        boardImage.setX(xOffset);
        boardImage.setY(yOffset);
        gameContext.getChildren().add(boardImage);

        die = new DiceRoller((float)imageSize/30);
        double diePositionInImage = (14 * imageSize)/30;
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
        gameContext.getChildren().add(rollButton);

        startSquares = new HashMap<>();
        pawns = new HashMap<>();

        JsonParser parser = new JsonParser();
        JsonObject positions = null;
        try {
            positions = (JsonObject)parser.parse(new InputStreamReader(
                    Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("data/horses/positions.json")),
                    "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        double scaleRatio = imageSize / boardImage.getImage().getHeight();
        Square loopBack = null;
        Square previousCommonSquare = null;
        for(TEAMS team: TEAMS.values()){
            JsonObject teamObject = (JsonObject)positions.get(team.name());
            JsonArray finalPath = (JsonArray)teamObject.get("finalPath");
            JsonArray commonPath = (JsonArray)teamObject.get("commonPath");

            Square firstFromFinal = null;
            Square previousSquare = null;
            for(int i = 0; i < finalPath.size(); i++){
                JsonObject object = (JsonObject)finalPath.get(i);
                Position position = new Position(xOffset + object.get("x").getAsDouble() * scaleRatio,
                        yOffset + object.get("y").getAsDouble() * scaleRatio);
                FinalPathSquare square = new FinalPathSquare(position, this, i + 1);
                square.setPreviousSquare(previousSquare);
                if(previousSquare != null){
                    previousSquare.setNextSquare(square);
                }
                if(i == 0){
                    firstFromFinal = square;
                }
                previousSquare = square;
            }

            for(int i = 0; i < commonPath.size(); i++){
                JsonObject object = (JsonObject)commonPath.get(i);
                Position position = new Position(xOffset + object.get("x").getAsDouble() * scaleRatio,
                        yOffset + object.get("y").getAsDouble() * scaleRatio);
                Square square = null;
                if(i == 0){
                    square = new FinalPathStart(position, this, team, firstFromFinal);
                    firstFromFinal.setPreviousSquare(square);
                }else{
                    square = new Square(position, this);
                }

                if(previousCommonSquare == null){
                    loopBack = square;
                }else{
                    previousCommonSquare.setNextSquare(square);
                    square.setPreviousSquare(previousCommonSquare);
                }

                if(i == 1){
                    startSquares.put(team, square);
                }
                if(team == TEAMS.YELLOW && i == commonPath.size() - 1){
                    square.setNextSquare(loopBack);
                }
                previousCommonSquare = square;
            }
        }

        ArrayList<Pawn> pawnList = new ArrayList();
        for(int i = 0; i < 1; i++){
            ProgressButton button = new ProgressButton();
            ImageView bibouleImage = new ImageView(String.format(BIBOULEPATH, "green"));
            bibouleImage.setFitHeight(imageSize / 15);
            bibouleImage.setFitWidth(imageSize / 15);
            button.setLayoutX(dimensions.getWidth() / 2 - rollImage.getFitWidth() / 2);
            button.setLayoutY(dimensions.getHeight() / 2 - rollImage.getFitHeight() / 2);
            button.setImage(bibouleImage);
            gameContext.getChildren().add(button);
            Pawn pawn = new Pawn(TEAMS.GREEN, button, new Position(imageSize/2, imageSize/2));
            pawn.setCurrentSquare(startSquares.get(TEAMS.RED));
            pawnList.add(pawn);
            pawns.put(TEAMS.GREEN, pawnList);
        }
    }

    private void roll(){
        rollButton.setOpacity(0);
        diceOutcome = die.roll(e -> playTurn());
    }

    private void playTurn(){
        pawns.get(TEAMS.GREEN).get(0).move(diceOutcome);
        rollButton.setOpacity(1);
    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}
