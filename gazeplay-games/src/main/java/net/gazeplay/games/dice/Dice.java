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
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.DiceRoller;
import net.gazeplay.components.ProgressButton;
import net.gazeplay.components.ProgressPane;

import java.util.ArrayList;

@Slf4j
public class Dice implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final GridPane gridpane;
    private ArrayList<DiceRoller> diceRollers;
    private Text totalText;
    private boolean active;
    private final int[] rolls;
    private final ProgressButton rollButton;
    private final Configuration configuration;

    private final ReplayablePseudoRandom randomGenerator;

    public Dice(final IGameContext gameContext, final Stats stats, final int nbDice) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
        final Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();
        active = true;

        rolls = new int[nbDice];

        // Roll button is used to roll all the dice at once
        rollButton = new ProgressButton();
        final ImageView nextImage = new ImageView("data/dice/roll.png");
        nextImage.setFitHeight(dimensions.getHeight() / 6);
        nextImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - nextImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * nextImage.getFitHeight());
        rollButton.setImage(nextImage);
        rollButton.assignIndicatorUpdatable(event -> {
            if (active) {
                active = false;
                totalText.setOpacity(0);
                for (int i = 0; i < diceRollers.size(); i++) {
                    rolls[i] = diceRollers.get(i).roll(i == 0 ? action -> addUp() : null);
                }
                stats.incrementNumberOfGoalsReached();
            }
        }, gameContext);
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        totalText = new Text(0, dimensions.getHeight() / 5, "");
        totalText.setTextAlignment(TextAlignment.CENTER);

        Color color = configuration.getBackgroundStyle().accept(new BackgroundStyleVisitor<Color>() {
            @Override
            public Color visitLight() {
                return Color.BLACK;
            }

            @Override
            public Color visitDark() {
                return Color.WHITE;
            }
        });

        totalText.setFill(color);

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
            final DiceRoller dr = new DiceRoller(dieWidth, gameContext.getSoundManager(), randomGenerator);
            diceRollers.add(dr);

            // init rolls to 1s
            rolls[i] = 1;

            // DiceRoller in a ProgressPane, so the dice can be rolled individually when gazed at
            final ProgressPane pp = new ProgressPane();
            pp.button.setCenter(dr);
            gridpane.add(pp, i, 0);
            final int finalI = i;
            pp.assignIndicator(e -> rolls[finalI] = dr.roll(action -> addUp()));
        }
    }

    public Dice(final IGameContext gameContext, final Stats stats, final int nbDice, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
        final Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        active = true;
        this.configuration = gameContext.getConfiguration();

        rolls = new int[nbDice];

        // Roll button is used to roll all the dice at once
        rollButton = new ProgressButton();
        final ImageView nextImage = new ImageView("data/dice/roll.png");
        nextImage.setFitHeight(dimensions.getHeight() / 6);
        nextImage.setFitWidth(dimensions.getHeight() / 6);
        rollButton.setLayoutX(dimensions.getWidth() / 2 - nextImage.getFitWidth() / 2);
        rollButton.setLayoutY(dimensions.getHeight() - 1.2 * nextImage.getFitHeight());
        rollButton.setImage(nextImage);
        rollButton.assignIndicatorUpdatable(event -> {
            if (active) {
                active = false;
                totalText.setOpacity(0);
                for (int i = 0; i < diceRollers.size(); i++) {
                    rolls[i] = diceRollers.get(i).roll(i == 0 ? action -> addUp() : null);
                }
                stats.incrementNumberOfGoalsReached();
            }
        }, gameContext);
        this.gameContext.getGazeDeviceManager().addEventFilter(rollButton);
        rollButton.active();

        totalText = new Text(0, dimensions.getHeight() / 5, "");
        totalText.setTextAlignment(TextAlignment.CENTER);

        Color color = configuration.getBackgroundStyle().accept(new BackgroundStyleVisitor<Color>() {
            @Override
            public Color visitLight() {
                return Color.BLACK;
            }

            @Override
            public Color visitDark() {
                return Color.WHITE;
            }
        });

        totalText.setFill(color);


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
            final DiceRoller dr = new DiceRoller(dieWidth, gameContext.getSoundManager(), randomGenerator);
            diceRollers.add(dr);

            // init rolls to 1s
            rolls[i] = 1;

            // DiceRoller in a ProgressPane, so the dice can be rolled individually when gazed at
            final ProgressPane pp = new ProgressPane();
            pp.button.setCenter(dr);
            gridpane.add(pp, i, 0);
            final int finalI = i;
            pp.assignIndicator(e -> rolls[finalI] = dr.roll(action -> addUp()));
        }
    }

    private void addUp() {
        int total = 0;
        for (final int roll : rolls) {
            total += roll;
        }
        totalText.setText("" + total);
        totalText.setOpacity(0);
        final Timeline showTotal = new Timeline(
            new KeyFrame(Duration.seconds(2), new KeyValue(totalText.opacityProperty(), 1)));
        showTotal.play();
        active = true;
    }

    @Override
    public void launch() {
        gameContext.getChildren().addAll(gridpane, totalText, rollButton);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {

    }
}
