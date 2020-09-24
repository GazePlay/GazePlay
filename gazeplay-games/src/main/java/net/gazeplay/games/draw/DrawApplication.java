package net.gazeplay.games.draw;

import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * @see http://java-buddy.blogspot.fr/2013/04/free-draw-on-javafx-canvas.html
 */
public class DrawApplication implements GameLifeCycle {

    private final Stats stats;
    private final IGameContext gameContext;
    private final ReplayablePseudoRandom randomGenerator;

    public DrawApplication(IGameContext gameContext, Stats stats) {
        this.stats  = stats;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        DrawBuilder drawBuilder = new DrawBuilder(randomGenerator);
        drawBuilder.setColorPicker(new RainbowColorPicker());

        final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();

        double coefficient = 1.5;

        Dimension2D canvasDimension = new Dimension2D(screenDimension.getWidth() / coefficient,
            screenDimension.getHeight() / coefficient);

        Canvas canvas = drawBuilder.createCanvas(canvasDimension, coefficient, this.stats);

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        root.prefWidthProperty().bind(gameContext.getRoot().widthProperty());
        root.prefHeightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getGazeDeviceManager().addEventFilter(canvas);
        gameContext.getChildren().add(canvas);
    }

    public DrawApplication(IGameContext gameContext, Stats stats, double gameSeed) {
        this.stats  = stats;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        DrawBuilder drawBuilder = new DrawBuilder(randomGenerator);
        drawBuilder.setColorPicker(new RainbowColorPicker());

        final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();

        double coefficient = 1.5;

        Dimension2D canvasDimension = new Dimension2D(screenDimension.getWidth() / coefficient,
            screenDimension.getHeight() / coefficient);

        Canvas canvas = drawBuilder.createCanvas(canvasDimension, coefficient, this.stats);

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        root.prefWidthProperty().bind(gameContext.getRoot().widthProperty());
        root.prefHeightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getGazeDeviceManager().addEventFilter(canvas);
        gameContext.getChildren().add(canvas);
    }

    @Override
    public void launch() {
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {

    }
}
