package net.gazeplay.games.draw;

import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DrawBuilder {

	@Setter
	private Color borderRectangleColor = Color.BLACK;

	@Setter
	private ColorPicker colorPicker = new RandomColorPicker();

	@Setter
	private int drawLineWidth = 8;

	public Canvas createCanvas(Dimension2D canvasDimension) {
		Canvas canvas = new Canvas(canvasDimension.getWidth(), canvasDimension.getHeight());
		final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		initDraw(graphicsContext);

		canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
				new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						graphicsContext.lineTo(event.getX(), event.getY());
						//graphicsContext.setStroke(colorPicker.pickColor());
						graphicsContext.stroke();
					}
				});

		canvas.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {


					@Override
					public void handle(MouseEvent event) {
						graphicsContext.setStroke(colorPicker.pickColor());
						graphicsContext.beginPath();
					}
				});

		canvas.addEventHandler(MouseEvent.MOUSE_EXITED,
				new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						graphicsContext.closePath();
					}
				});
		return canvas;
	}

	private void initDraw(GraphicsContext gc) {
		double canvasWidth = gc.getCanvas().getWidth();
		double canvasHeight = gc.getCanvas().getHeight();

		log.info("canvasWidth = {}, canvasHeight = {}", canvasWidth, canvasHeight);

		gc.clearRect(0, 0, canvasWidth, canvasHeight);

		gc.setStroke(borderRectangleColor);
		gc.setLineWidth(5);
		gc.strokeRect(
				0,              //x of the upper left corner
				0,              //y of the upper left corner
				canvasWidth,    //width of the rectangle
				canvasHeight);  //height of the rectangle

		gc.setFill(Color.RED);
		gc.setStroke(Color.BLUE);
		gc.setLineWidth(drawLineWidth);
	}

	public void clear(Canvas canvas) {
		initDraw(canvas.getGraphicsContext2D());
	}

}
