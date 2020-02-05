package pictogrammes;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by schwab on 23/08/2016.
 */
public class Pictos extends Parent {

    private final double min_time = Math.sqrt(2) * 1000;
    private final float zoom_factor = 1.1f;

    boolean found = false;

    long entry = -1;

    HashMap<String, Picto> pictos;

    final ArrayList<Picto> currentPictos;

    private final Scene scene;

    final EventHandler<Event> enterEvent;

    public Pictos(final Scene scene, final GazeDeviceManager gazeDeviceManager) {

        this.scene = scene;

        buildPictos();

        enterEvent = buildEvent();

        currentPictos = new ArrayList<>(2);

        // Picto R1 = pictos.get("compote");
        final Picto R1 = pictos.get("minnie");

        final Picto R2 = pictos.get("bulles");

        currentPictos.add(R1);
        currentPictos.add(R2);

        // log.info(scene.getWidth());
        // log.info(scene.getHeight());
        final int sep = 100;
        final int min_X = 100;
        final double imagesWidth = scene.getWidth() / 2 - min_X / 2d - sep / 2d;
        final int min_Y = 50;
        final double imagesHeight = scene.getHeight() - min_Y * 2;

        for (int i = 0; i < currentPictos.size(); i++) {

            final Picto R = currentPictos.get(i);

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

        /*
         * R1.setTranslateX(min_X); R1.setTranslateY(min_Y); R1.setWidth(imagesWidth); R1.setHeight(imagesHeight);
         * R1.setStrokeWidth(imagesWidth*strokeFactor);
         *
         * this.getChildren().add(R1);
         *
         * R2.setTranslateX(min_X + imagesWidth + sep); R2.setTranslateY(min_Y); R2.setWidth(imagesWidth);
         * R2.setHeight(imagesHeight); R2.setStrokeWidth(imagesWidth*strokeFactor);
         *
         * this.getChildren().add(R2);
         */
        // GazeDeviceManagerFactory.addEventFilter(R1);
        // GazeDeviceManagerFactory.addEventFilter(R2);

        /*
         * R1.addEventFilter(MouseEvent.ANY, enterEvent); R1.addEventFilter(GazeEvent.ANY, enterEvent);
         * R2.addEventFilter(MouseEvent.ANY, enterEvent); R2.addEventFilter(GazeEvent.ANY, enterEvent);
         */
    }

    private void buildPictos() {

        pictos = new HashMap<>(10);

        // R1.setFill(new ImagePattern(new Image("file:images/bulles.jpg"), 0, 0, 1, 1, true));

        pictos.put("bulles", new Picto("bulles"));

        // R2.setFill(new ImagePattern(new Image("file:images/compote.jpg"), 0, 0, 1, 1, true));

        // pictos.put("compote", new Picto("file:images/compote.jpg", "file:sounds/compote.m4a"));

        pictos.put("tambour", new Picto("tambour"));

        pictos.put("minnie", new Picto("minnie"));
    }

    private EventHandler<Event> buildEvent() {
        return e -> {

            final Rectangle target = (Rectangle) e.getTarget();

            // log.debug("Rectangle " + Target.getTranslateX());
            // log.debug(e.getEventType());

            if (found) {

                return;
            }

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                // log.debug("ENTRÉE");
                entry = (new Date()).getTime();

                int i;
                for (i = 0; i < currentPictos.size() && !target.equals(currentPictos.get(i).rectangle); i++) {
                }

                if (i < currentPictos.size()) {
                    currentPictos.get(i).sound.play();
                }

            } else if (e.getEventType() == GazeEvent.GAZE_MOVED || e.getEventType() == MouseEvent.MOUSE_MOVED) {

                // log.debug("MOVE");

                final long now = (new Date()).getTime();

                if (entry != -1 && (now - entry) > min_time) {

                    // log.debug("GAGNÉ");

                    found = true;

                    final double finalWidth = target.getWidth() * zoom_factor;

                    final double finalHeight = target.getHeight() * zoom_factor;

                    final Timeline timeline = new Timeline();

                    timeline.getKeyFrames().add(
                        new KeyFrame(new Duration(1000), new KeyValue(target.heightProperty(), finalHeight)));
                    timeline.getKeyFrames().add(
                        new KeyFrame(new Duration(1000), new KeyValue(target.widthProperty(), finalWidth)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                        new KeyValue(target.translateXProperty(), (scene.getWidth() - finalWidth) / 2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                        new KeyValue(target.translateYProperty(), (scene.getHeight() - finalHeight) / 2)));

                    for (final Picto P : currentPictos) {

                        if (!P.rectangle.equals(target)) {
                            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                new KeyValue(P.rectangle.opacityProperty(), 0)));
                        }

                    }

                    timeline.play();

                } else {

                    final Timeline timeline = new Timeline();

                    // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.heightProperty(),
                    // R1.getHeight() * zoom_factor)));
                    // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.widthProperty(),
                    // R1.getWidth() * zoom_factor)));
                    timeline.getKeyFrames()
                        .add(new KeyFrame(new Duration(1), new KeyValue(target.strokeProperty(), Color.RED)));

                    timeline.play();

                    // log.debug("DESSUS " + (now - entry));

                }
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                final Timeline timeline = new Timeline();

                // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.heightProperty(),
                // R1.getHeight() * zoom_factor)));
                // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.widthProperty(),
                // R1.getWidth() * zoom_factor)));
                timeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(target.strokeProperty(), Color.BLACK)));

                timeline.play();

                // log.debug("SORTIE");
                entry = -1;
            }
        };

    }

    static class Picto {

        final Rectangle rectangle;

        AudioClip sound;

        public Picto(final String name) {

            this.rectangle = new Rectangle();

            final String soundResourceName = "pictogrammes/sounds/" + name + ".m4a";
            final URL soundSourceResource = getClass().getClassLoader().getResource(soundResourceName);
            if (soundSourceResource == null) {
                throw new RuntimeException("Resource not found : " + soundResourceName);
            }

            this.sound = new AudioClip(soundSourceResource.toExternalForm());

            final String imageResourceName = "pictogrammes/images/" + name + ".jpg";
            final URL imageResource = getClass().getClassLoader().getResource(imageResourceName);
            if (imageResource == null) {
                throw new RuntimeException("Resource not found : " + imageResourceName);
            }
            rectangle.setFill(new ImagePattern(new Image(imageResource.toExternalForm()), 0, 0, 1, 1, true));
        }

    }

}
