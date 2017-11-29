package net.gazeplay.games.whereisit;

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
import net.gazeplay.utils.Bravo;
import net.gazeplay.utils.Home;
import net.gazeplay.utils.HomeUtils;
import utils.games.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt extends Application {

    public static final int ANIMALNAME = 0;
    public static final int COLORNAME = 1;

    private static Group root;
    private static Scene scene;
    protected static int nbLines;
    protected static int nbColumns;
    protected static String pathSound;
    protected static int type;
    protected static boolean fourThree;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Where is it ?");

        primaryStage.setFullScreen(true);

        Group appRoot = new Group();

        Scene theScene = new Scene(appRoot, Screen.getScreens().get(0).getWidth(), Screen.getScreens().get(0).getHeight(),
                Color.BLACK);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(theScene);

        WhereIsItStats stats = new WhereIsItStats(scene);

        buildGame(ANIMALNAME, 2, 2, false, root, scene, null, stats);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    public static void buildGame(int Game, int nLines, int nColumns, boolean fourThree, Group groupRoot,
            Scene gameScene, ChoiceBox choicebox, WhereIsItStats stats) {

        root = groupRoot;
        scene = gameScene;
        WhereIsIt.nbLines = nLines;
        WhereIsIt.nbColumns = nColumns;
        WhereIsIt.type = Game;
        WhereIsIt.fourThree = fourThree;

        stats.setName(getName());

        double shift = 0;

        Rectangle2D bounds = javafx.stage.Screen.getPrimary().getBounds();

        double screenWidth = bounds.getWidth();
        double screenHeight = bounds.getHeight();

        double width = 0, height = 0;

        log.info("16/9 or 16/10 screen ? = "+((screenWidth / screenHeight)-(16.0/9.0)));

        if (fourThree && ((screenWidth / screenHeight)-(16.0/9.0)) < 0.1) {

            width = 4 * screenHeight / 3;
            height = screenHeight;
            shift = (screenWidth - width) / 2;
        } else {

            width = screenWidth;
            height = screenHeight;
            shift = 0;
        }

        width = width / nbColumns;
        height = height / nbLines;

        int nbImages = nbLines * nbColumns;

        int winner = (int) (nbImages * Math.random());

        URL url = WhereIsIt.class.getResource("data/" + getName() + "/images/");

        try {
            url = new URL("file:data/" + getName() + "/images/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        log.info(url + "");

        // System.exit(0);

        File F = new File(url.getFile());

        log.info("F exists " + F.exists());

        File[] folders = F.listFiles();

        log.info("Dir = " + F.isDirectory());

        log.info("File[] folders = " + folders);

        int deb = (int) (folders.length * Math.random());

        int step = 1;//(int) (Math.random() + 1.5);

        log.info("deb " + deb);
        log.info("step " + step);

        log.info("folders[deb] " + folders[deb]);

        int posX = 0;
        int posY = 0;

        for (int i = 0; i < nbImages; i++) {

            int index = (deb + step * i) % folders.length;

            File[] files = folders[(index) % folders.length].listFiles();

            int numFile = (int) (files.length * Math.random());

            log.info(files[numFile] + "");

            if (winner == i) {
                pathSound = getPathSound(folders[(index) % folders.length].getName(), (new Configuration()).language);
                Utils.playSound(pathSound);
            }

            Pictures picture = new Pictures(width * posX + shift, height * posY, width, height, root, scene,
                    winner == i, files[numFile] + "", choicebox, stats);

            log.info("posX " + posX);
            log.info("posY " + posY);

            if ((i + 1) % nbColumns != 0)
                posX++;
            else {
                posY++;
                posX = 0;
            }

            root.getChildren().add(picture);
        }

        stats.start();
    }

    public static String getPathSound(String folder, String language) {

        if (language.equals("deu"))// erase when translation is complete
            language = "eng";

        String voice = "w";

        if (Math.random() > 0.5)
            voice = "m";

        return "data/" + getName() + "/sounds/" + language + "/" + folder + "." + voice + "." + language + ".mp3";
    }

    private static String getName(){

        switch(type){

            case ANIMALNAME :
                return "where-is-the-animal";

            case COLORNAME :
                return "where-is-the-color";

            default:
                log.debug("This case should never happen");
                System.exit(0); // should never happen
                return null;
        }
    }
}

@Slf4j
class Pictures extends Group {

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
    private WhereIsItStats stats;
    private Scene scene;
    private ChoiceBox choicebox;

    EventHandler<Event> enterEvent;

    Bravo bravo = Bravo.getBravo();

    public Pictures(double posX, double posY, double width, double height, Group root, Scene scene, boolean winner,
            String imagePath, ChoiceBox choicebox, WhereIsItStats stats) {

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
        indicator.setTranslateX(RImage.getX() + width / 8);
        indicator.setTranslateY(RImage.getY() + height / 8);
        indicator.setMinWidth(width * 0.75);
        indicator.setMinHeight(height * 0.75);
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

                                    if ((N instanceof Pictures && RImage != ((Pictures) N).RImage
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
                                                WhereIsIt.buildGame(WhereIsIt.type, WhereIsIt.nbLines,
                                                        WhereIsIt.nbColumns, WhereIsIt.fourThree, root, scene,
                                                        choicebox, stats);
                                                HomeUtils.home(scene, root, choicebox, stats);
                                                // stats.start();
                                            }
                                        });
                                    }
                                });

                                timeline.play();

                            } else {// bad card

                                Timeline disparition = new Timeline();
                                Timeline apparition = new Timeline();

                                disparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(2000), new KeyValue(RImage.opacityProperty(), 0)));

                                disparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(2000), new KeyValue(RImage.fillProperty(),new ImagePattern(new Image("data/common/images/error.png"),  0, 0,1, 1, true))));

                                apparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(1), new KeyValue(RImage.widthProperty(), initHeight/2)));

                                apparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(1), new KeyValue(RImage.heightProperty(), initHeight/2)));

                                apparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(1), new KeyValue(RImage.layoutXProperty(), initWidth/3)));

                                apparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(1), new KeyValue(RImage.layoutYProperty(), initHeight/4)));

                                apparition.getKeyFrames().add(
                                        new KeyFrame(new Duration(2000), new KeyValue(RImage.opacityProperty(), 0.8)));

                                SequentialTransition sq = new SequentialTransition();
                                sq.getChildren().addAll(disparition,apparition);
                                sq.play();



                                Utils.playSound(WhereIsIt.pathSound);

                                indicator.setOpacity(0);
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    /*
                     * Timeline timeline = new Timeline();
                     * 
                     * timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(RImage.xProperty(),
                     * RImage.getX() + (initWidth * zoom_factor - initWidth) / 2))); timeline.getKeyFrames().add(new
                     * KeyFrame(new Duration(1), new KeyValue(RImage.yProperty(), RImage.getY() + (initHeight *
                     * zoom_factor - initHeight) / 2))); timeline.getKeyFrames() .add(new KeyFrame(new Duration(1), new
                     * KeyValue(RImage.widthProperty(), initWidth))); timeline.getKeyFrames() .add(new KeyFrame(new
                     * Duration(1), new KeyValue(RImage.heightProperty(), initHeight)));
                     * 
                     * timeline.play();
                     */

                    timelineProgressBar.stop();

                    indicator.setOpacity(0);
                    indicator.setProgress(0);
                }
            }
        };
    }

}
