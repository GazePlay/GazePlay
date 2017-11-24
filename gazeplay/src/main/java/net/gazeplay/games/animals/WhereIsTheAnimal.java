package net.gazeplay.games.animals;

//It is repeated always, it works like a charm :)

import com.sun.glass.ui.Screen;
import gaze.GazeEvent;
import gaze.GazeUtils;
import gaze.SecondScreen;
import gaze.configuration.Configuration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.games.magiccards.Card;
import net.gazeplay.utils.Bravo;
import net.gazeplay.utils.Home;
import net.gazeplay.utils.HomeUtils;
import utils.games.Utils;

import java.io.File;
import java.util.Random;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsTheAnimal extends Application {

    private static Group root;
    private static Scene scene;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Where is the animal ?");

        primaryStage.setFullScreen(true);

        root = new Group();

        scene = new Scene(root, Screen.getScreens().get(0).getWidth(), Screen.getScreens().get(0).getHeight(),
                Color.BLACK);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        AnimalStats stats = new AnimalStats(scene);

        buildGame(root, scene, null, stats);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    public static void buildGame(Group groupRoot, Scene gameScene, ChoiceBox choicebox, AnimalStats stats) {

        root = groupRoot;
        scene = gameScene;

        Rectangle2D bounds = javafx.stage.Screen.getPrimary().getBounds();

        double width = bounds.getWidth();
        double height = bounds.getHeight();

        int winner = (int) (4 * Math.random());

        File F = new File("data/animals/images/");

        log.info("F exists " + F.exists());

        File[] folders = F.listFiles();

        log.info("Dir = " + F.isDirectory());

        log.info("File[] folders = " + folders);

        int deb = (int) (folders.length * Math.random());

        int step = (int) (2 * Math.random() + 1);

        log.info("deb " + deb);
        log.info("step " + step);

        log.info("folders[deb] " + folders[deb]);

        int i = (deb + step * 0) % folders.length;
        ;

        File[] files = folders[(i) % folders.length].listFiles();

        int numFile = (int) (files.length * Math.random());

        log.info(files[numFile] + "");

        if (winner == 0)
            Utils.playSound("data/animals/sounds/fra/" + folders[(i) % folders.length].getName() + ".w.fra.mp3");

        AnimalPicture R1 = new AnimalPicture(0, 0, width / 2, height / 2, root, scene, winner == 0, files[numFile] + "",
                choicebox, stats);

        i = (deb + step * 1) % folders.length;
        ;

        files = folders[(i) % folders.length].listFiles();

        numFile = (int) (files.length * Math.random());

        log.info(files[numFile] + "");

        if (winner == 1)
            Utils.playSound("data/animals/sounds/fra/" + folders[(i) % folders.length].getName() + ".w.fra.mp3");

        AnimalPicture R2 = new AnimalPicture(width / 2, 0, width / 2, height / 2, root, scene, winner == 1,
                files[numFile] + "", choicebox, stats);

        i = (deb + step * 2) % folders.length;

        files = folders[i].listFiles();

        numFile = (int) (files.length * Math.random());

        log.info(files[numFile] + "");

        if (winner == 2)
            Utils.playSound("data/animals/sounds/fra/" + folders[(i) % folders.length].getName() + ".w.fra.mp3");

        AnimalPicture R3 = new AnimalPicture(0, height / 2, width / 2, height / 2, root, scene, winner == 2,
                files[numFile] + "", choicebox, stats);

        i = (deb + step * 3) % folders.length;

        files = folders[i].listFiles();

        numFile = (int) (files.length * Math.random());

        log.info(files[numFile] + "");

        if (winner == 3)
            Utils.playSound("data/animals/sounds/fra/" + folders[(i) % folders.length].getName() + ".w.fra.mp3");

        AnimalPicture R4 = new AnimalPicture(width / 2, height / 2, width / 2, height / 2, root, scene, winner == 3,
                files[numFile] + "", choicebox, stats);

        /*
         * AnimalPicture R1 = new AnimalPicture(0, 0, width / 2, height / 2, root, scene, winner1,
         * "data/animals/images/bees/bee-1040521_1280.jpg", choicebox, stats); AnimalPicture R2 = new
         * AnimalPicture(width / 2, 0, width / 2, height / 2, root, scene, winner2,
         * "data/animals/images/cats/cat-1337527_1280.jpg", choicebox, stats); AnimalPicture R3 = new AnimalPicture(0,
         * height / 2, width / 2, height / 2, root, scene, winner3,
         * "data/animals/images/crocodiles/animal-194914_1280.jpg", choicebox, stats); AnimalPicture R4 = new
         * AnimalPicture(width / 2, height / 2, width / 2, height / 2, root, scene, winner4,
         * "data/animals/images/horses/horse-2572051_1280.jpg", choicebox, stats);
         */

        root.getChildren().addAll(R1, R2, R3, R4);
    }
}

@Slf4j
class AnimalPicture extends Group {

    protected static final float zoom_factor = 1.1f;
    private boolean selected;
    ProgressIndicator indicator;
    private double minTime;
    private Group root;
    private boolean winner;
    private Rectangle RImage;
    private Timeline timelineProgressBar;
    private double initWidth;
    private double initHeight;
    private AnimalStats stats;
    private Scene scene;
    private ChoiceBox choicebox;

    EventHandler<Event> enterEvent;

    Bravo bravo = Bravo.getBravo();

    public AnimalPicture(double posX, double posY, double width, double height, Group root, Scene scene, boolean winner,
            String imagePath, ChoiceBox choicebox, AnimalStats stats) {

        minTime = new Configuration().fixationlength;
        this.initWidth = width;
        this.initHeight = height;
        selected = false;
        this.winner = winner;
        this.root = root;
        this.stats = stats;
        this.scene = scene;
        this.choicebox = choicebox;
        RImage = new Rectangle(posX, posY, width, height);
        this.getChildren().add(RImage);
        RImage.setFill(new ImagePattern(new Image(imagePath), 0, 0, 1, 1, true));
        indicator = new ProgressIndicator(0);
        indicator.setTranslateX(RImage.getX() + width / 4);
        double ratio = width / height;
        log.info("Ratio " + ratio);
        indicator.setTranslateY(RImage.getY() + height - width / 1.8);
        indicator.setMinWidth(width * 0.5);
        indicator.setMinHeight(width * 0.5);
        indicator.setOpacity(0);
        this.getChildren().add(indicator);

        enterEvent = buildEvent();

        GazeUtils.addEventFilter(this);

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (selected)
                    return;

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    indicator.setOpacity(0.5);
                    indicator.setProgress(0);

                    Timeline timelineCard = new Timeline();

                /*    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(RImage.xProperty(),
                            RImage.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(RImage.yProperty(),
                            RImage.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(RImage.widthProperty(), initWidth * zoom_factor)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(RImage.heightProperty(), initHeight * zoom_factor)));*/

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames()
                            .add(new KeyFrame(new Duration(minTime), new KeyValue(indicator.progressProperty(), 1)));

                    timelineCard.play();

                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {

                            selected = true;

                            RImage.removeEventFilter(MouseEvent.ANY, enterEvent);
                            RImage.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (winner) {

                                stats.incNbGoals();

                                int final_zoom = 2;

                                indicator.setOpacity(0);

                                Timeline timeline = new Timeline();

                                for (Node N : root.getChildren()) {// clear all but images and reward

                                    log.info(N + "");

                                    if ((N instanceof AnimalPicture && RImage != ((AnimalPicture) N).RImage
                                            && !(N instanceof Bravo)) || (N instanceof Home)) {// we put outside screen
                                                                                               // Home and cards

                                        log.info(N + " enlevé ");
                                        N.setTranslateX(-10000);
                                        N.setOpacity(0);
                                        N.removeEventFilter(MouseEvent.ANY, enterEvent);
                                        N.removeEventFilter(GazeEvent.ANY, enterEvent);
                                    } else {// we keep only Bravo and winning card
                                    }
                                }

                                timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                        new KeyValue(RImage.widthProperty(), RImage.getWidth() * final_zoom)));
                                timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                        new KeyValue(RImage.heightProperty(), RImage.getHeight() * final_zoom)));
                                timeline.getKeyFrames()
                                        .add(new KeyFrame(new Duration(1000), new KeyValue(RImage.xProperty(),
                                                (scene.getWidth() - RImage.getWidth() * final_zoom) / 2)));
                                timeline.getKeyFrames()
                                        .add(new KeyFrame(new Duration(1000), new KeyValue(RImage.yProperty(),
                                                (scene.getHeight() - RImage.getHeight() * final_zoom) / 2)));

                                timeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {

                                        SequentialTransition sequence = bravo.win();
                                        bravo.toFront();
                                        sequence.setOnFinished(new EventHandler<ActionEvent>() {

                                            @Override
                                            public void handle(ActionEvent actionEvent) {
                                                HomeUtils.clear(scene, root, choicebox);
                                                WhereIsTheAnimal.buildGame(root, scene, choicebox, stats);
                                                HomeUtils.home(scene, root, choicebox, stats);
                                                stats.start();
                                            }
                                        });
                                    }
                                });

                                timeline.play();

                            } else {// bad card

                                Timeline timeline = new Timeline();

                                timeline.getKeyFrames().add(
                                        new KeyFrame(new Duration(2000), new KeyValue(RImage.opacityProperty(), 0)));

                                timeline.play();

                                indicator.setOpacity(0);
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

             /*       Timeline timeline = new Timeline();

                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(RImage.xProperty(),
                            RImage.getX() + (initWidth * zoom_factor - initWidth) / 2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(RImage.yProperty(),
                            RImage.getY() + (initHeight * zoom_factor - initHeight) / 2)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(RImage.widthProperty(), initWidth)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(RImage.heightProperty(), initHeight)));

                    timeline.play();*/

                    timelineProgressBar.stop();

                    indicator.setOpacity(0);
                    indicator.setProgress(0);
                }
            }
        };
    }

}
