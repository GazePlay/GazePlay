package net.gazeplay.games.dice;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
import net.gazeplay.commons.utils.DiceRoll;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class Dice implements GameLifeCycle {

    private GameContext gameContext;
    private Stats stats;
    private DiceRoll diceRoll;
    private Dimension2D dimensions;
    private int[] rolls;
    private Text totalText;
    private boolean active;
    private ProgressButton rollButton;

    public Dice(GameContext gameContext, Stats stats, int nbDice) {
        this.gameContext = gameContext;
        this.stats = stats;
        diceRoll = new DiceRoll(gameContext, nbDice);
        dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        active = true;

        Configuration config = Configuration.getInstance();

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
                rolls = diceRoll.roll(action -> addUp());
            }
        }, config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        totalText = new Text(0, dimensions.getHeight() / 5, "");
        totalText.setTextAlignment(TextAlignment.CENTER);
        totalText.setFill(Color.WHITE);
        totalText.setFont(new Font(dimensions.getHeight() / 4));
        totalText.setWrappingWidth(dimensions.getWidth());
    }

    private void addUp() {
        int total = 0;
        for (int roll : rolls) {
            total += roll;
        }
        totalText.setText("" + total);
        Timeline showTotal = new Timeline(
                new KeyFrame(Duration.seconds(2), new KeyValue(totalText.opacityProperty(), 1)));
        showTotal.play();
        showTotal.setOnFinished(e -> active = true);
    }

    @Override
    public void launch() {
        gameContext.getChildren().addAll(diceRoll, totalText, rollButton);
    }

    @Override
    public void dispose() {

    }
}
