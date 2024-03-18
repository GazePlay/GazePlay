package net.gazeplay.games.bubbles;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.RandomPositionGenerator;
import net.gazeplay.games.blocs.Blocs;

import java.util.ArrayList;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Bubble implements GameLifeCycle {

    public static final String DIRECTION_TOP = "ToTop";
    public static final String DIRECTION_BOTTOM = "ToBottom";
    public static final String DIRECTION_LEFT = "ToLeft";
    public static final String DIRECTION_RIGHT = "ToRight";
    public static final String FIX = "Fix";

    private final IGameContext gameContext;

    private final BubbleType type;
    private ArrayList<Bloc> blocs;

    private final Stats stats;


    private final BubblesGameVariant gameVariant;

    private final ReplayablePseudoRandom randomGenerator;

    public Bubble(final IGameContext gameContext, final BubbleType type, final Stats stats, final BubblesGameVariant gameVariant) {
        super();
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.blocs = new ArrayList<>();
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
            double imageRectangleOpacity = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<>() {
                @Override
                public Double visitLight() {
                    return 0.1;
                }

                @Override
                public Double visitDark() {
                    return 1.d;
                }
            });

            int randomWallpaperIndex = randomGenerator.nextInt(3);
            switch (randomWallpaperIndex) {
                case 1 -> imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/inhabited-ocean.png")));
                case 2 -> imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/empty-ocean.png")));
                default -> imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/underwater-treasures.jpg")));
            }
            imageRectangle.setOpacity(imageRectangleOpacity);

            gameContext.getChildren().add(imageRectangle);

            int nbColomns = 6;
            int nbLines = 6;
            final double width = dimension2D.getWidth() / nbColomns;
            final double height = dimension2D.getHeight() / nbLines;
            for (int i = 0; i < nbColomns; i++) {
                for (int j = 0; j < nbLines; j++) {
                    final Bloc bloc = new Bloc(i * width, j * height, width + 1, height + 1, i, j);// width+1, height+1 to avoid
                    bloc.setFill(Color.BLACK);
                    gameContext.getChildren().add(bloc);
                    bloc.toFront();
                    blocs.add(bloc);

                }
            }
        }
    }

    @Override
    public void launch() {
        initBackground(true);
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();
        randomPositionGenerator.setRandomGenerator(randomGenerator);
        for (int i = 0; i < 10; i++) {
            Target portrait = new Target(gameContext, randomPositionGenerator, stats,
                Portrait.createImageLibrary(randomGenerator), gameVariant, this, randomGenerator, type, blocs);
            gameContext.getChildren().add(portrait);
        }
        gameContext.setLimiterAvailable();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.incrementNumberOfGoalsToReach();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

    public static class Bloc extends Rectangle {

        final int posX;
        final int posY;

        Bloc(final double x, final double y, final double width, final double height, final int posX, final int posY) {
            super(x, y, width, height);
            this.posX = posX;
            this.posY = posY;
        }

    }
}
