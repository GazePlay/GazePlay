package net.gazeplay.games.draw;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * @web http://java-buddy.blogspot.fr/2013/04/free-draw-on-javafx-canvas.html
 */
public class DrawApplication implements GameLifeCycle {

    public DrawApplication(GameContext gameContext, Stats stats) {
        DrawBuilder drawBuilder = new DrawBuilder();
        drawBuilder.setColorPicker(new RainbowColorPicker());

        Rectangle2D bounds = Screen.getPrimary().getBounds();

        double coefficient = 1.5;

        Dimension2D canvasDimension = new Dimension2D(bounds.getWidth() / coefficient,
                bounds.getHeight() / coefficient);

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
