package net.gazeplay.games.draw;

import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * @see http://java-buddy.blogspot.fr/2013/04/free-draw-on-javafx-canvas.html
 */
public class DrawApplication implements GameLifeCycle {

    public DrawApplication(IGameContext gameContext, Stats stats) {
        DrawBuilder drawBuilder = new DrawBuilder();
        drawBuilder.setColorPicker(new RainbowColorPicker());

        final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();

        double coefficient = 1.5;

        Dimension2D canvasDimension = new Dimension2D(screenDimension.getWidth() / coefficient,
            screenDimension.getHeight() / coefficient);

        Canvas canvas = drawBuilder.createCanvas(canvasDimension, coefficient);

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        root.prefWidthProperty().bind(gameContext.getRoot().widthProperty());
        root.prefHeightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getGazeDeviceManager().addEventFilter(canvas);
        gameContext.getChildren().add(canvas);

    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}
