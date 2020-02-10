package net.gazeplay.commons.gaze;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

/**
 * Created by schwab on 10/09/2016.
 */
public class Lighting extends Parent {

    private final Rectangle pixel;
    private final int lightingLength;
    private final Color lightingColor;

    Lighting(int x, int y, int pixelWidth, int lightingLength, Color lightingColor) {
        this.lightingLength = lightingLength * 1000;
        this.lightingColor = lightingColor;
        pixel = new Rectangle(x, y, pixelWidth, pixelWidth);
        // pixel.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
        pixel.setFill(Color.BLACK);
        this.getChildren().add(pixel);

        EventHandler<Event> enterEvent = e -> {
            if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == MouseEvent.MOUSE_MOVED
                || e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == GazeEvent.GAZE_MOVED)) {
                enter();
            }
        };
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventHandler(GazeEvent.ANY, enterEvent);
    }

    public void enter() {

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(pixel.fillProperty(), lightingColor)));
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(lightingLength), new KeyValue(pixel.fillProperty(), Color.BLACK)));

        SequentialTransition sequence = new SequentialTransition(timeline, timeline2);

        sequence.play();
    }

}
