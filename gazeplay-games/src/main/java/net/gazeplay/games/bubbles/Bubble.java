package net.gazeplay.games.bubbles;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Bubble implements GameLifeCycle {

    public static final String DIRECTION_TOP = "toTop";
    public static final String DIRECTION_BOTTOM = "toBottom";
    public static final String DIRECTION_LEFT = "toLeft";
    public static final String DIRECTION_RIGHT = "toRight";

    private final IGameContext gameContext;

    private final BubbleType type;

    private final Stats stats;

    private Target portrait;

    private final BubblesGameVariant gameVariant;

    private final ReplayablePseudoRandom randomGenerator;

    public Bubble(final IGameContext gameContext, final BubbleType type, final Stats stats, final BubblesGameVariant gameVariant) {
        super();
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public Bubble(final IGameContext gameContext, final BubbleType type, final Stats stats, final BubblesGameVariant gameVariant, double gameSeed) {
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    void initBackground(boolean useBackgroundImage) {
        if (useBackgroundImage && gameContext.getConfiguration().isBackgroundEnabled()) {
            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            double imageRectangleOpacity = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Double>() {
                @Override
                public Double visitLight() {
                    return 0.1;
                }

                @Override
                public Double visitDark() {
                    return 1.d;
                }
            });

            imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/underwater-treasures.jpg")));
            imageRectangle.setOpacity(imageRectangleOpacity);

            gameContext.getChildren().add(imageRectangle);
        }
    }

    @Override
    public void launch() {
        initBackground(true);
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();
        randomPositionGenerator.setRandomGenerator(randomGenerator);

        for (int i = 0; i < 10; i++) {
            portrait = new Target(gameContext, randomPositionGenerator, stats,
                Portrait.createImageLibrary(randomGenerator), gameVariant, this, randomGenerator, type);
        }
        gameContext.setLimiterAvailable();
        gameContext.getChildren().add(portrait);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.incrementNumberOfGoalsToReach();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }
}
