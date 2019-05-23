package net.gazeplay.games.dice;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.DiceRoller;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.ProgressPane;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

@Slf4j
public class Dice implements GameLifeCycle {

    private GameContext gameContext;
    private Stats stats;
    private GridPane gridpane;
    private ArrayList<DiceRoller> diceRollers;
    private Dimension2D dimensions;
    private Text totalText;
    private boolean active;
    private int[] rolls;
    private ProgressButton rollButton;

    public Dice(GameContext gameContext, Stats stats, int nbDice) {
        this.gameContext = gameContext;
        this.stats = stats;
        dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        active = true;

        rolls = new int[nbDice];

        Configuration config = Configuration.getInstance();

        // Roll button is used to roll all the dice at once
        rollButton = new ProgressButton();
        ImageView nextImage = new ImageView("data/dice/roll.png");
        nextImage.setFitHeight(dimensions.getHeight() / 6);
        nextImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - nextImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * nextImage.getFitHeight());
        rollButton.setImage(nextImage);
        rollButton.assignIndicator(event -> {
            if (active) {
                active = false;
                totalText.setOpacity(0);
                for (int i = 0; i < diceRollers.size(); i++) {
                    rolls[i] = diceRollers.get(i).roll(i == 0 ? action -> addUp() : null);
                }
            }
        }, config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        totalText = new Text(0, dimensions.getHeight() / 5, "");
        totalText.setTextAlignment(TextAlignment.CENTER);
        totalText.setFill(Color.WHITE);
        totalText.setFont(new Font(dimensions.getHeight() / 4));
        totalText.setWrappingWidth(dimensions.getWidth());

        gridpane = new GridPane();
        gridpane.setMinSize(dimensions.getWidth(), dimensions.getHeight());
        gridpane.setHgap((dimensions.getWidth() / nbDice) * 0.4);
        gridpane.setAlignment(Pos.CENTER);

        diceRollers = new ArrayList<>();
        float dieWidth = (float) ((dimensions.getWidth() / nbDice) * 0.6) / 2;
        if (dieWidth > dimensions.getHeight() / 4) {
            dieWidth = (float) (dimensions.getHeight() / 4);
        }
        for (int i = 0; i < nbDice; i++) {
            DiceRoller dr = new DiceRoller(dieWidth);
            diceRollers.add(dr);

            // init rolls to 1s
            rolls[i] = 1;

            // DiceRoller in a ProgressPane, so the dice can be rolled individually when gazed at
            ProgressPane pp = new ProgressPane();
            pp.button.setCenter(dr);
            gridpane.add(pp, i, 0);
            int finalI = i;
            pp.assignIndicator(e -> {
                rolls[finalI] = dr.roll(action -> addUp());
            });
        }
    }

    private void addUp() {
        int total = 0;
        for (int i = 0; i < rolls.length; i++) {
            total += rolls[i];
        }
        totalText.setText("" + total);
        totalText.setOpacity(0);
        Timeline showTotal = new Timeline(
                new KeyFrame(Duration.seconds(2), new KeyValue(totalText.opacityProperty(), 1)));
        showTotal.play();
        active = true;
    }

    @Override
    public void launch() {
        gameContext.getChildren().addAll(gridpane, totalText, rollButton);
    }

    @Override
    public void dispose() {

    }
}
