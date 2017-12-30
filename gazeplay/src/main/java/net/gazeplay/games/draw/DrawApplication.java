package net.gazeplay.games.draw;

import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @web http://java-buddy.blogspot.fr/2013/04/free-draw-on-javafx-canvas.html
 */
public class DrawApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	Dimension2D canvasDimension = new Dimension2D(800, 800);

	@Override
	public void start(Stage primaryStage) {
		DrawBuilder drawBuilder = new DrawBuilder();
		drawBuilder.setColorPicker(new RainbowColorPicker());
		Canvas canvas = drawBuilder.createCanvas(canvasDimension);

		StackPane root = new StackPane();
		root.getChildren().add(canvas);
		Scene scene = new Scene(root, canvasDimension.getWidth(), canvasDimension.getHeight());

		primaryStage.setTitle(this.getClass().getSimpleName());
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
		primaryStage.show();
	}


}
