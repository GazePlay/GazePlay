package arduino.windmill;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import utils.arduino.ArduinoSerialCommunication;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by schwab on 23/10/2016.
 */
public class Choices extends Parent {

    private final double min_time = 500;// Math.sqrt(2) * 1000;
    private final float zoom_factor = 1.1f;

    String current = "";

    long entry = -1;

    HashMap<String, Choice> choices;

    final ArrayList<Choice> currentChoice;

    final EventHandler<Event> enterEvent;

    final ArduinoSerialCommunication arduino;

    public Choices(final Scene scene, final GazeDeviceManager gazeDeviceManager) {

        arduino = new ArduinoSerialCommunication();
        arduino.initialize();

        buildPictos();

        enterEvent = buildEvent();

        currentChoice = new ArrayList<>(2);

        final Choice R1 = choices.get("oui");

        final Choice R2 = choices.get("non");

        currentChoice.add(R1);
        currentChoice.add(R2);

        final double sep = 100d;
        final double min_X = 100d;
        final double imagesWidth = scene.getWidth() / 2 - min_X / 2d - sep / 2d;
        final double min_Y = 50d;
        final double imagesHeight = scene.getHeight() - min_Y * 2d;

        for (int i = 0; i < currentChoice.size(); i++) {

            final Choice R = currentChoice.get(i);

            R.rectangle.setTranslateX(min_X + (sep + imagesWidth) * i);
            R.rectangle.setTranslateY(min_Y);
            R.rectangle.setWidth(imagesWidth);
            R.rectangle.setHeight(imagesHeight);
            final float strokeFactor = 0.01f;
            R.rectangle.setStrokeWidth(imagesWidth * strokeFactor);

            this.getChildren().add(R.rectangle);

            gazeDeviceManager.addEventFilter(R.rectangle);

            R.rectangle.addEventFilter(MouseEvent.ANY, enterEvent);
            R.rectangle.addEventFilter(GazeEvent.ANY, enterEvent);
        }

    }

    private void buildPictos() {

        choices = new HashMap<>(10);

        choices.put("oui", new Choice("oui"));

        choices.put("non", new Choice("non"));
    }

    private EventHandler<Event> buildEvent() {
        return e -> {

            final Rectangle target = (Rectangle) e.getTarget();

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                entry = (new Date()).getTime();

                int i;
                for (i = 0; i < currentChoice.size() && !target.equals(currentChoice.get(i).rectangle); i++) {
                }

                if (i < currentChoice.size()) {
                    currentChoice.get(i).sound.play();
                }

                final Timeline timeline = new Timeline();

                timeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(target.strokeProperty(), Color.RED)));

                timeline.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                final Timeline timeline = new Timeline();

                timeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(target.strokeProperty(), Color.BLACK)));

                timeline.play();

                entry = -1;
            } else if (e.getEventType() == GazeEvent.GAZE_MOVED || e.getEventType() == MouseEvent.MOUSE_MOVED) {

                // log.info("MOVE");

                final long now = (new Date()).getTime();

                if (entry != -1 && (now - entry) > min_time) {

                    for (final Choice P : currentChoice) {

                        if (P.rectangle.equals(target)) {

                            if (P.name.equals("oui")) {

                                if (!current.equals("oui")) {

                                    arduino.sendArduino("M");
                                    current = "oui";
                                }
                            } else if (!current.equals("non")) {

                                arduino.sendArduino("L");
                                current = "non";
                            }
                        }

                    }
                }
            }
        };

    }
}

class Choice {

    final Rectangle rectangle;

    final AudioClip sound;

    final String name;

    public Choice(final String name) {

        this.rectangle = new Rectangle();
        this.sound = new AudioClip("file:sounds/" + name + ".m4a");
        rectangle.setFill(new ImagePattern(new Image("file:images/" + name + ".png"), 0, 0, 1, 1, true));
        this.name = name;
    }

}
