package net.gazeplay.games.drawonvideo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.games.draw.DrawBuilder;
import net.gazeplay.games.draw.ProgressiveColorPicker;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerWithLiveFeedbackApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private static final String videoId;

	Dimension2D videoDimension = new Dimension2D(1280, 720); // 720p

	Dimension2D canvasDimension = new Dimension2D(videoDimension.getWidth(), videoDimension.getHeight() - 80);

	double canvasOpacityWhilePlaying = 0.1d;

	double canvasOpacityWhileOutside = 0.7d;

	/**
	 * configure the frequency of the canvas switch, when using 3 canvas
	 */
	long canvasSwitchFrequency = 500;

	int canvasCount = 12;

	static {
		videoId = "YE7VzlLtp-4"; // big buck bunny
	}

	@Override
	public void start(Stage stage) throws Exception {

		String videoUrl = "http://www.youtube.com/embed/" + videoId + "?autoplay=1";

		WebView webview = new WebView();
		webview.getEngine().load(videoUrl);
		webview.setPrefSize(videoDimension.getWidth(), videoDimension.getHeight());

		DrawBuilder drawBuilder = new DrawBuilder();
		drawBuilder.setBorderRectangleColor(Color.RED);
		drawBuilder.setDrawLineWidth(64);
		drawBuilder.setColorPicker(new ProgressiveColorPicker());

		List<Canvas> canvasList = new ArrayList<>();
		for (int i = 0; i < canvasCount; i++) {
			canvasList.add(drawBuilder.createCanvas(canvasDimension));
		}

		StackPane root = new StackPane();
		root.getChildren().add(webview);
		root.getChildren().addAll(canvasList);

		Scene scene = new Scene(root, videoDimension.getWidth(), videoDimension.getHeight());
		stage.setScene(scene);

		for (Canvas canvas : canvasList) {
			canvas.setOpacity(canvasOpacityWhilePlaying);
		}

		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				// set the canvas to invisible
				// in order to allow mouse interaction with the WebView (YouTube controls)
				// the canvas will be set visible when the mouse exit then re-enter the scene
				for (Canvas canvas : canvasList) {
					canvas.setVisible(false);
				}
			}
		});
		scene.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				for (Canvas canvas : canvasList) {
					canvas.setOpacity(canvasOpacityWhileOutside);
				}
			}
		});
		scene.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				for (Canvas canvas : canvasList) {
					drawBuilder.clear(canvas);
					canvas.setOpacity(canvasOpacityWhilePlaying);
					canvas.setVisible(true);
				}
			}
		});

		Runnable canvasSwitchingTask = new Runnable() {

			int activeCanvasIndex = 0;

			int nextCanvasIndex = activeCanvasIndex + 1;

			Canvas nextCanvas = null;

			Canvas activeCanvas = null;

			Canvas previousCanvas = null;

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(canvasSwitchFrequency);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}

					activeCanvasIndex++;
					if (activeCanvasIndex >= canvasList.size()) {
						activeCanvasIndex = 0;
					}
					nextCanvasIndex = activeCanvasIndex + 1;
					if (nextCanvasIndex >= canvasList.size()) {
						nextCanvasIndex = 0;
					}

					previousCanvas = activeCanvas;
					activeCanvas = canvasList.get(activeCanvasIndex);
					nextCanvas = canvasList.get(nextCanvasIndex);

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							activeCanvas.toFront();
							if (nextCanvas != null) {
								if (nextCanvas != activeCanvas) {
									drawBuilder.clear(nextCanvas);
								}
							}
						}
					});
				}
			}
		};
		Thread canvasSwitchingThread = new Thread(canvasSwitchingTask);

		stage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
		stage.show();
		stage.setTitle(this.getClass().getSimpleName());

		canvasSwitchingThread.start();
	}

}
