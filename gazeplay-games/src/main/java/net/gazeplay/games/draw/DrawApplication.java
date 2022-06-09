package net.gazeplay.games.draw;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
        this.stats = stats;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        DrawBuilder drawBuilder = new DrawBuilder(randomGenerator);
        drawBuilder.setColorPicker(new RainbowColorPicker());

        final Scene scene = gameContext.getPrimaryScene();

        double coefficient = 1.5;

        Canvas canvas = drawBuilder.createCanvas(scene, coefficient, this.stats);

        StackPane root = new StackPane();

        root.setPrefWidth(scene.getWidth());
        root.setPrefHeight(scene.getHeight());

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        root.minWidthProperty().bind(scene.widthProperty());
        root.minHeightProperty().bind(scene.heightProperty());
        root.maxWidthProperty().bind(scene.widthProperty());
        root.maxHeightProperty().bind(scene.heightProperty());


        Rectangle border = new Rectangle(canvas.getWidth(), canvas.getHeight());
        border.setStrokeWidth(5);
        border.setStroke(Color.WHITE);
        border.setFill(Color.BLACK);

        border.widthProperty().bind(canvas.widthProperty());
        border.heightProperty().bind(canvas.heightProperty());

        root.getChildren().addAll(border, canvas);
        gameContext.getGazeDeviceManager().addEventFilter(canvas);
        gameContext.getChildren().addAll(root);
    }

    public DrawApplication(IGameContext gameContext, Stats stats, double gameSeed) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        DrawBuilder drawBuilder = new DrawBuilder(randomGenerator);
        drawBuilder.setColorPicker(new RainbowColorPicker());

        final Scene scene = gameContext.getPrimaryScene();

        double coefficient = 1.5;

        Canvas canvas = drawBuilder.createCanvas(scene, coefficient, this.stats);

        StackPane root = new StackPane();

        root.setPrefWidth(scene.getWidth());
        root.setPrefHeight(scene.getHeight());

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        root.minWidthProperty().bind(scene.widthProperty());
        root.minHeightProperty().bind(scene.heightProperty());
        root.maxWidthProperty().bind(scene.widthProperty());
        root.maxHeightProperty().bind(scene.heightProperty());


        Rectangle border = new Rectangle(canvas.getWidth(), canvas.getHeight());
        border.setStrokeWidth(5);
        border.setStroke(Color.WHITE);
        border.setFill(Color.BLACK);

        border.widthProperty().bind(canvas.widthProperty());
        border.heightProperty().bind(canvas.heightProperty());

        root.getChildren().addAll(border, canvas);
        gameContext.getGazeDeviceManager().addEventFilter(canvas);
        gameContext.getChildren().addAll(root);
    }

    @Override
    public void launch() {
        gameContext.setOffFixationLengthControl();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {

    }
}
