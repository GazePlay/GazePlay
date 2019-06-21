package net.gazeplay.games.goosegame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
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

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;
    private final Multilinguism translate;

    private ArrayList<DiceRoller> diceRollers;
    private Timeline moveDiceIn;
    private Timeline moveDiceOut;
    private GridPane diceDisplay;
    private int[] rolls;
    private ProgressButton rollButton;

    private Text messageText;
    private Timeline showMessage;

    private JsonArray positions;

    private ArrayList<Pawn> pawns;
    private int currentPawn;
    private Square firstSquare;

    public GooseGame(GameContext gameContext, Stats stats, int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();
        this.translate = Multilinguism.getSingleton();

        Rectangle background = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        background.setFill(Color.GRAY);
        gameContext.getChildren().add(background);

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

        ArrayList<Integer> repeatSquares = new ArrayList<>(Arrays.asList(5, 9, 13, 18, 24, 28, 34, 36, 40, 45, 49, 54));
        Square previousSquare = null;
        BridgeSquare beginBridge = null;
        for(int i = 0; i < 64; i++){
            JsonObject jsonPos = (JsonObject)positions.get(i);
            Position position = new Position(jsonPos.get("x").getAsDouble(), jsonPos.get("y").getAsDouble());

            Square newSquare;
            if(repeatSquares.contains(i)){
                newSquare = new RepeatSquare(i, position, previousSquare, this);
            }else if(i == 31 || i == 52){
                newSquare = new PrisonSquare(i, position, previousSquare, this);
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
        String colors[] = {"Blue", "Orange", "green"};

        for(int i = 0; i < nbPlayers; i++){
            ImageView imagePawn = new ImageView("data/biboulejump/biboules/" + colors[i] + ".png");
            imagePawn.setFitHeight(dimensions.getWidth()/20);
            imagePawn.setFitWidth(dimensions.getWidth()/20);
            gameContext.getChildren().add(imagePawn);
            pawns.add(new Pawn(imagePawn, firstSquare));
        }

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
        gameContext.getChildren().add(diceDisplay);

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

        gameContext.getChildren().add(rollButton);

        messageText = new Text(0, dimensions.getHeight() / 3, "");
        messageText.setTextAlignment(TextAlignment.CENTER);
        messageText.setFill(Color.BLUE);
        messageText.setFont(new Font(dimensions.getHeight() / 10));
        messageText.setWrappingWidth(dimensions.getWidth());
        messageText.setOpacity(0);

        gameContext.getChildren().add(messageText);

        showMessage = new Timeline(
                new KeyFrame(Duration.seconds(0.3), new KeyValue(messageText.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(4), new KeyValue(messageText.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(4.3), new KeyValue(messageText.opacityProperty(), 0))
                );
    }

    private void roll(){
        rollButton.disable();
        rollButton.setOpacity(0);
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
        for(Pawn pawn: pawns){
            pawn.reset(firstSquare);
        }
        currentPawn = 0;
    }

    @Override
    public void dispose() {

    }

    public void endOfTurn(){
        rollButton.active();
        rollButton.setOpacity(1);
        int i = 0;
        do {
            currentPawn = (currentPawn + 1) % nbPlayers;
            i++;
        }while(!pawns.get(currentPawn).canPlay() && i <= nbPlayers);

        if(i >= nbPlayers){
            //draw
        }

    }

    public void showMessage(String message){
        messageText.setText(translate.getTrad(message, config.getLanguage()));
        showMessage.playFromStart();
    }

    void winner(Pawn pawn) {
        gameContext.playWinTransition(200, actionEvent -> {
            try {
                gameContext.showRoundStats(stats, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
