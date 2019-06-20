package net.gazeplay.games.goosegame;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.DiceRoller;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.ProgressPane;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

@Slf4j
public class GooseGame implements GameLifeCycle {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 7;

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;

    private ArrayList<DiceRoller> diceRollers;
    private GridPane diceDisplay;
    private int[] rolls;
    private ProgressButton rollButton;

    private JsonArray positions;

    private Pawn pawn;
    private Square firstSquare;

    public GooseGame(GameContext gameContext, Stats stats, int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();

        ImageView boardImage = new ImageView("data/goosegame/gooseboard.png");
        gameContext.getChildren().add(boardImage);

        JsonParser parser = new JsonParser();
        try {
            positions = (JsonArray) parser.parse(new InputStreamReader(
                    new FileInputStream("gazeplay-data/src/main/resources/data/goosegame/positions.json"),
                    "utf-8"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Square previousSquare = null;
        for(int i = 0; i < 63; i++){
            JsonObject jsonPos = (JsonObject)positions.get(i);
            Position position = new Position(jsonPos.get("x").getAsDouble(), jsonPos.get("y").getAsDouble());
            Square newSquare = new Square(i + 1, position, previousSquare, this);
            if(previousSquare != null){
                previousSquare.setNextSquare(newSquare);
            }else{
                firstSquare = newSquare;
            }
            previousSquare = newSquare;
        }

        ImageView imagePawn = new ImageView("data/biboulejump/biboules/Blue.png");
        imagePawn.setFitHeight(dimensions.getWidth()/20);
        imagePawn.setFitWidth(dimensions.getWidth()/20);
        gameContext.getChildren().add(imagePawn);
        pawn = new Pawn(imagePawn, firstSquare);

        diceDisplay = new GridPane();
        diceDisplay.setHgap(dimensions.getWidth() / 20);
        diceDisplay.setOpacity(0);
        rolls = new int[2];
        diceRollers = new ArrayList<>();
        float dieWidth = (float) (dimensions.getWidth() / 12);

        for (int i = 0; i < 2; i++) {
            DiceRoller dr = new DiceRoller(dieWidth);
            diceRollers.add(dr);
            diceDisplay.add(dr, i, 0);
            // init rolls to 1s
            rolls[i] = 1;
        }

        gameContext.getChildren().add(diceDisplay);

        rollButton = new ProgressButton();
        ImageView rollImage = new ImageView("data/dice/roll.png");
        rollImage.setFitHeight(dimensions.getHeight() / 6);
        rollImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - rollImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollImage.getFitHeight());
        rollButton.setImage(rollImage);
        rollButton.assignIndicator(event -> {
            diceDisplay.setOpacity(1);
            for (int i = 0; i < diceRollers.size(); i++) {
                rolls[i] = diceRollers.get(i).roll(i == 0 ? action -> playTurn() : null);
            }
        }, config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        gameContext.getChildren().add(rollButton);
    }

    private void playTurn(){
        diceDisplay.setOpacity(0);
        int rollResult = rolls[0] + rolls[1];
        pawn.setLastThrowResult(rollResult);
        pawn.getCurrentSquare().moveForward(pawn, rollResult);
    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}
