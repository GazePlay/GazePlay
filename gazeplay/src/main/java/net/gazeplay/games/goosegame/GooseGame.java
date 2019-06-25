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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
public class GooseGame implements GameLifeCycle {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 7;
    private static final String BIBOULEPATH = "data/biboulejump/biboules/%s.png";

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;
    @Getter
    private final Multilinguism translate;

    private Rectangle background;
    private ImageView boardImage;
    private ArrayList<String> bibouleColors;

    private ArrayList<DiceRoller> diceRollers;
    private Timeline moveDiceIn;
    private Timeline moveDiceOut;
    private GridPane diceDisplay;
    private int[] rolls;
    private ProgressButton rollButton;

    private VBox messages;

    private JsonArray positions;

    private ArrayList<Pawn> pawns;
    private int currentPawn;
    private Square firstSquare;

    private ImageView turnIndicator;
    private Text turnText;

    public GooseGame(GameContext gameContext, Stats stats, int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();
        this.translate = Multilinguism.getSingleton();

        JsonParser parser = new JsonParser();
        try {
            positions = (JsonArray) parser.parse(new InputStreamReader(
                    new FileInputStream("gazeplay-data/src/main/resources/data/goosegame/positions.json"),
                    "utf-8"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        boardImage = new ImageView("data/goosegame/gooseboard.png");
        double scaleRatio = Math.min((dimensions.getHeight() * 0.9) / boardImage.getImage().getHeight(), (dimensions.getWidth() * 0.9) / boardImage.getImage().getWidth());
        double boardWidth = boardImage.getImage().getWidth() * scaleRatio;
        double boardHeight = boardImage.getImage().getHeight() * scaleRatio;
        boardImage.setFitHeight(boardHeight);
        boardImage.setFitWidth(boardWidth);
        double xOffset = (dimensions.getWidth() - boardWidth)/2;
        double yOffset = (dimensions.getHeight() - boardHeight)/2;
        boardImage.setX(xOffset);
        boardImage.setY(yOffset);

        ArrayList<Integer> repeatSquares = new ArrayList<>(Arrays.asList(5, 9, 13, 18, 24, 28, 34, 36, 40, 45, 49, 54));
        Square previousSquare = null;
        BridgeSquare beginBridge = null;
        for(int i = 0; i < 64; i++){
            JsonObject jsonPos = (JsonObject)positions.get(i);
            Position position = new Position(xOffset+jsonPos.get("x").getAsDouble() * scaleRatio, yOffset+jsonPos.get("y").getAsDouble() * scaleRatio);

            Square newSquare;
            if(repeatSquares.contains(i)){
                newSquare = new RepeatSquare(i, position, previousSquare, this);
            }else if(i == 31 || i == 52){
                newSquare = new PrisonSquare(i, position, previousSquare, this, i == 31? "Player %d fell into a well" : "Player %d got locked up in prison");
            }else if(i == 19){
                newSquare = new SkipSquare(i, position, previousSquare, this, 2);
            }else if(i == 58){
                newSquare = new RestartSquare(i, position, previousSquare, this, firstSquare);
            }else if(i == 7){
                beginBridge = new BridgeSquare(i, position, previousSquare, this);
                newSquare = beginBridge;
            }else if(i == 63){
                newSquare = new EndSquare(i, position, previousSquare, this);
            }else{
                 newSquare = new Square(i, position, previousSquare, this);
            }

            if(i == 32){
                beginBridge.setDestinationSquare(newSquare);
            }

            if(previousSquare != null){
                previousSquare.setNextSquare(newSquare);
            }else{
                firstSquare = newSquare;
            }
            previousSquare = newSquare;
        }

        pawns = new ArrayList<>();
        bibouleColors = new ArrayList<>(Arrays.asList("Blue", "Orange", "green", "Yellow", "Red"));
        for(int i = 0; i < nbPlayers; i++){
            ImageView imagePawn = new ImageView(String.format(BIBOULEPATH, bibouleColors.get(i)));
            imagePawn.setFitHeight(dimensions.getWidth()/20);
            imagePawn.setFitWidth(dimensions.getWidth()/20);
            pawns.add(new Pawn(imagePawn, firstSquare, i + 1));
        }

        turnIndicator = new ImageView(String.format(BIBOULEPATH, bibouleColors.get(0)));
        turnIndicator.setFitHeight(dimensions.getWidth()/12);
        turnIndicator.setFitWidth(dimensions.getWidth()/12);
        Text turnText = new Text("");

        diceDisplay = new GridPane();
        diceDisplay.setHgap(dimensions.getWidth() / 20);
        rolls = new int[2];
        diceRollers = new ArrayList<>();
        float dieWidth = (float) (dimensions.getWidth() / 20);

        for (int i = 0; i < 2; i++) {
            DiceRoller dr = new DiceRoller(dieWidth);
            diceRollers.add(dr);
            diceDisplay.add(dr, i, 0);
            // init rolls to 1s
            rolls[i] = 1;
        }

        moveDiceIn = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(diceDisplay.layoutXProperty(), dimensions.getWidth()/2 - 3*dieWidth, Interpolator.EASE_OUT),
                        new KeyValue(diceDisplay.layoutYProperty(), dimensions.getHeight()/2 - dieWidth , Interpolator.EASE_OUT),
                        new KeyValue(diceDisplay.scaleXProperty(), 1),
                        new KeyValue(diceDisplay.scaleYProperty(), 1)));
        moveDiceOut = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(diceDisplay.layoutXProperty(), -dieWidth, Interpolator.EASE_OUT),
                        new KeyValue(diceDisplay.layoutYProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(diceDisplay.scaleXProperty(), 0.5),
                        new KeyValue(diceDisplay.scaleYProperty(), 0.5)));

        diceDisplay.setScaleX(0.5);
        diceDisplay.setScaleY(0.5);
        diceDisplay.setLayoutX(-dieWidth);

        rollButton = new ProgressButton();
        ImageView rollImage = new ImageView("data/dice/roll.png");
        rollImage.setFitHeight(dimensions.getHeight() / 6);
        rollImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - rollImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollImage.getFitHeight());
        rollButton.setImage(rollImage);
        rollButton.assignIndicator(event -> roll(), config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        messages = new VBox();
        messages.setAlignment(Pos.CENTER);
    }

    private void roll(){
        rollButton.setLayoutY(dimensions.getHeight()*2);
        moveDiceIn.playFromStart();
        moveDiceIn.setOnFinished(e -> {
            for (int i = 0; i < diceRollers.size(); i++) {
                rolls[i] = diceRollers.get(i).roll(i == 0 ? action -> playTurn() : null);
            }
        });
    }

    private void playTurn(){
        moveDiceOut.playFromStart();
        int rollResult = rolls[0] + rolls[1];
        pawns.get(currentPawn).move(rollResult);
    }

    @Override
    public void launch() {
        background = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        background.setFill(Color.GRAY);
        turnIndicator.setX(11*dimensions.getWidth()/12 - dimensions.getWidth()/25);
        turnIndicator.setY(dimensions.getWidth()/25);

        gameContext.getChildren().addAll(background, boardImage, turnIndicator);

        for(Pawn pawn: pawns){
            pawn.reset(firstSquare);
            gameContext.getChildren().add(pawn.getPawnDisplay());
        }
        currentPawn = 0;

        gameContext.getChildren().addAll(diceDisplay, rollButton, messages);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().remove(gameContext.getChildren().size()-1);
        for(Pawn pawn: pawns) {
            pawn.reset(firstSquare);
        }
        currentPawn = 0;
        turnIndicator.setImage(new Image(String.format(BIBOULEPATH, bibouleColors.get(currentPawn))));
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollButton.getImage().getFitHeight());
    }

    private boolean isGameStuck(){
        int i = 0;
        while(i < pawns.size() && pawns.get(i).isStuck()){
            i++;
        }
        return i == pawns.size();
    }

    public void endOfTurn(){
        if(isGameStuck()){
            showMessage("STUCK");
        } else {
            rollButton.setLayoutY(dimensions.getHeight() - 1.2 * rollButton.getImage().getFitHeight());
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
            showMessage("Player %d's turn", pawn.getNumber());
        }
    }

    public void showMessage(String message, Object... values){
        Text messageText = new Text(0, dimensions.getHeight() / 3, String.format(translate.getTrad(message, config.getLanguage()), values));
        messageText.setTextAlignment(TextAlignment.CENTER);
        messageText.setFill(Color.WHITE);
        messageText.setFont(new Font(dimensions.getHeight() / 10));
        messageText.setWrappingWidth(dimensions.getWidth());
        messageText.setOpacity(0);

        messages.getChildren().add(messageText);

        Timeline showMessage = new Timeline(
                new KeyFrame(Duration.seconds(0.3), new KeyValue(messageText.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(4), new KeyValue(messageText.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(4.3), new KeyValue(messageText.opacityProperty(), 0))
        );

        showMessage.setOnFinished(e -> {
            messages.getChildren().remove(messageText);
        });

        showMessage.playFromStart();
    }

    void winner(Pawn pawn) {
        showMessage("Player %d wins the game", pawn.getNumber());
        gameContext.playWinTransition(200, actionEvent -> {
            dispose();
        });
    }
}
