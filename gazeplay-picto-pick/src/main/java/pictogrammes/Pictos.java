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

    private final int min_X = 100;
    private final int min_Y = 50;
    private final int sep = 100;

    private final double min_time = Math.sqrt(2) * 1000;
    private final float zoom_factor = 1.1f;
    private final float strokeFactor = 0.01f;

    boolean found = false;

    long entry = -1;

    HashMap<String, Picto> pictos;

    ArrayList<Picto> currentPictos;

    private Scene scene;

    EventHandler<Event> enterEvent;

    public Pictos(Scene scene, GazeDeviceManager gazeDeviceManager) {

        this.scene = scene;

        buildPictos();

        enterEvent = buildEvent();

        currentPictos = new ArrayList<Picto>(2);

        // Picto R1 = pictos.get("compote");
        Picto R1 = pictos.get("minnie");

        Picto R2 = pictos.get("bulles");

        currentPictos.add(R1);
        currentPictos.add(R2);

        // log.info(scene.getWidth());
        // log.info(scene.getHeight());
        double imagesWidth = scene.getWidth() / 2 - min_X / 2 - sep / 2;
        double imagesHeight = scene.getHeight() - min_Y * 2;

        for (int i = 0; i < currentPictos.size(); i++) {

            Picto R = currentPictos.get(i);

            R.rectangle.setTranslateX(min_X + (sep + imagesWidth) * i);
            R.rectangle.setTranslateY(min_Y);
            R.rectangle.setWidth(imagesWidth);
            R.rectangle.setHeight(imagesHeight);
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

        pictos = new HashMap<String, Picto>(10);

        // R1.setFill(new ImagePattern(new Image("file:images/bulles.jpg"), 0, 0, 1, 1, true));

        pictos.put("bulles", new Picto("bulles"));

        // R2.setFill(new ImagePattern(new Image("file:images/compote.jpg"), 0, 0, 1, 1, true));

        // pictos.put("compote", new Picto("file:images/compote.jpg", "file:sounds/compote.m4a"));

        pictos.put("tambour", new Picto("tambour"));

        pictos.put("minnie", new Picto("minnie"));
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                Rectangle target = (Rectangle) e.getTarget();

                // log.info("Rectangle " + Target.getTranslateX());
                // log.info(e.getEventType());

                if (found) {

                    return;
                }

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    // log.info("ENTRÉE");
                    entry = (new Date()).getTime();

                    int i;
                    for (i = 0; i < currentPictos.size() && !target.equals(currentPictos.get(i).rectangle); i++)
                        ;

                    if (i < currentPictos.size())
                        currentPictos.get(i).sound.play();

                } else if (e.getEventType() == GazeEvent.GAZE_MOVED || e.getEventType() == MouseEvent.MOUSE_MOVED) {

                    // log.info("MOVE");

                    long now = (new Date()).getTime();

                    if (entry != -1 && (now - entry) > min_time) {

                        // log.info("GAGNÉ");

                        found = true;

                        double finalWidth = target.getWidth() * zoom_factor;

                        double finalHeight = target.getHeight() * zoom_factor;

                        Timeline timeline = new Timeline();

                        timeline.getKeyFrames().add(
                                new KeyFrame(new Duration(1000), new KeyValue(target.heightProperty(), finalHeight)));
                        timeline.getKeyFrames().add(
                                new KeyFrame(new Duration(1000), new KeyValue(target.widthProperty(), finalWidth)));
                        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                new KeyValue(target.translateXProperty(), (scene.getWidth() - finalWidth) / 2)));
                        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                new KeyValue(target.translateYProperty(), (scene.getHeight() - finalHeight) / 2)));

                        for (Picto P : currentPictos) {

                            if (!P.rectangle.equals(target))
                                timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                        new KeyValue(P.rectangle.opacityProperty(), 0)));

                        }

                        timeline.play();

                    } else {

                        Timeline timeline = new Timeline();

                        // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.heightProperty(),
                        // R1.getHeight() * zoom_factor)));
                        // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.widthProperty(),
                        // R1.getWidth() * zoom_factor)));
                        timeline.getKeyFrames()
                                .add(new KeyFrame(new Duration(1), new KeyValue(target.strokeProperty(), Color.RED)));

                        timeline.play();

                        // log.info("DESSUS " + (now - entry));

                    }
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();

                    // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.heightProperty(),
                    // R1.getHeight() * zoom_factor)));
                    // timeline.getKeyFrames().add(new KeyFrame(new Duration(500),new KeyValue(R1.widthProperty(),
                    // R1.getWidth() * zoom_factor)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(target.strokeProperty(), Color.BLACK)));

                    timeline.play();

                    // log.info("SORTIE");
                    entry = -1;
                }
            }
        };

    }

    class Picto {

        Rectangle rectangle;

        AudioClip sound;

        public Picto(String name) {

            this.rectangle = new Rectangle();

            String soundResourceName = "pictogrammes/sounds/" + name + ".m4a";
            URL soundSourceResource = getClass().getClassLoader().getResource(soundResourceName);
            if (soundSourceResource == null) {
                throw new RuntimeException("Resource not found : " + soundResourceName);
            }

            this.sound = new AudioClip(soundSourceResource.toExternalForm());

            String imageResourceName = "pictogrammes/images/" + name + ".jpg";
            URL imageResource = getClass().getClassLoader().getResource(imageResourceName);
            if (imageResource == null) {
                throw new RuntimeException("Resource not found : " + imageResourceName);
            }
            rectangle.setFill(new ImagePattern(new Image(imageResource.toExternalForm()), 0, 0, 1, 1, true));
        }

    }

}
