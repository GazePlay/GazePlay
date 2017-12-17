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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.utils.Bravo;
import net.gazeplay.utils.Home;
import net.gazeplay.utils.HomeUtils;
import net.gazeplay.utils.multilinguism.Multilinguism;
import utils.games.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt extends Application {

    public static final int ANIMALNAME = 0;
    public static final int COLORNAME = 1;
    public static final int CUSTOMIZED = 2;

    private static Group root;
    private static Scene scene;
    private static ChoiceBox choicebox;
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

        Scene theScene = new Scene(appRoot, Screen.getScreens().get(0).getWidth(),
                Screen.getScreens().get(0).getHeight(), Color.BLACK);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(theScene);

        WhereIsItStats stats = new WhereIsItStats(scene);

        buildGame(ANIMALNAME, 2, 2, false, root, scene, null, stats);

        primaryStage.show();

        SecondScreen.launch();
    }

    public static void buildGame(int Game, int nLines, int nColumns, boolean fourThree, Group groupRoot,
            Scene gameScene, ChoiceBox gpChoicebox, WhereIsItStats stats) {

        root = groupRoot;
        scene = gameScene;
        choicebox = gpChoicebox;
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

        log.info("16/9 or 16/10 screen ? = " + ((screenWidth / screenHeight) - (16.0 / 9.0)));

        if (fourThree && ((screenWidth / screenHeight) - (16.0 / 9.0)) < 0.1) {

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
        final int nbImages = nbLines * nbColumns;
        log.debug("nbImages = {}", nbImages);
        Random r = new Random();
        final int winner = r.nextInt(nbImages);
        log.debug("winner = {}", winner);

        final File imagesDirectory = locateImagesDirectory();

        final File[] imagesFolders = imagesDirectory.listFiles();
        final int filesCount = imagesFolders == null ? 0 : imagesFolders.length;

        if (filesCount == 0) {
            log.info("No image found in Directory " + imagesDirectory);
            error();
            return;
        }

        log.info("imagesFolders = {}", imagesFolders);

        final int randomFolderIndex = r.nextInt(filesCount);
        log.info("randomFolderIndex " + randomFolderIndex);

        int step = 1; // (int) (Math.random() + 1.5);
        log.info("step " + step);

        log.info("imagesFolders[randomFolderIndex] " + imagesFolders[randomFolderIndex]);

        int posX = 0;
        int posY = 0;

        for (int i = 0; i < nbImages; i++) {

            final int index = (randomFolderIndex + step * i) % filesCount;

            final File[] files = imagesFolders[(index) % filesCount].listFiles();

            final int numFile = r.nextInt(files.length);

            final File randomImageFile = files[numFile];
            log.info("randomImageFile = {}", randomImageFile);

            if (winner == i) {

                log.info("randomImageFile.getAbsolutePath() " + randomImageFile.getAbsolutePath());
                pathSound = getPathSound(imagesFolders[(index) % filesCount].getName(), (new Configuration()).language);
                log.info("pathSound = {}", pathSound);
                Utils.playSound(pathSound);
            }

            Pictures picture = new Pictures(width * posX + shift, height * posY, width, height, root, scene,
                    winner == i, randomImageFile + "", choicebox, stats);

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

    private static void error() {

        HomeUtils.clear(scene, root, choicebox);
        HomeUtils.home(scene, root, choicebox, null);

        Multilinguism multilinguism = Multilinguism.getMultilinguism();
        Text error = new Text(multilinguism.getTrad("WII-error", Multilinguism.getLanguage()));
        error.setX(Screen.getMainScreen().getWidth() / 2. - 100);
        error.setY(Screen.getMainScreen().getHeight() / 2.);
        error.setId("item");
        root.getChildren().addAll(error);
    }

    private static File locateImagesDirectory() {

        File result = null;

        if (type == CUSTOMIZED) {

            result = new File((new Configuration()).getWhereIsItDir() + "/images/");
        } else {

            result = locateImagesDirectoryInUnpackedDistDirectory();

            if (result == null) {
                result = locateImagesDirectoryInExplodedClassPath();
            }
        }
        return result;
    }

    private static File locateImagesDirectoryInUnpackedDistDirectory() {
        final File workingDirectory = new File(".");
        log.info("workingDirectory = {}", workingDirectory.getAbsolutePath());
        final String workingDirectoryName;
        try {
            workingDirectoryName = workingDirectory.getCanonicalFile().getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("workingDirectoryName = {}", workingDirectoryName);

        final String parentImagesPackageResourceLocation = "data/" + getName() + "/images/";
        log.info("parentImagesPackageResourceLocation = {}", parentImagesPackageResourceLocation);

        {
            final File imagesDirectory = new File(workingDirectory, parentImagesPackageResourceLocation);
            log.info("imagesDirectory = {}", imagesDirectory.getAbsolutePath());
            boolean checked = checkImageDirectory(imagesDirectory);
            if (checked) {
                return imagesDirectory;
            }
        }

        if (workingDirectoryName.equals("bin")) {
            final File imagesDirectory = new File(workingDirectory, "../" + parentImagesPackageResourceLocation);
            log.info("imagesDirectory = {}", imagesDirectory.getAbsolutePath());
            boolean checked = checkImageDirectory(imagesDirectory);
            if (checked) {
                return imagesDirectory;
            }
        }

        return null;
    }

    private static File locateImagesDirectoryInExplodedClassPath() {
        final String parentImagesPackageResourceLocation = "data/" + getName() + "/images/";
        log.info("parentImagesPackageResourceLocation = {}", parentImagesPackageResourceLocation);

        final URL parentImagesPackageResourceUrl;

        final ClassLoader classLoader = WhereIsIt.class.getClassLoader();
        parentImagesPackageResourceUrl = classLoader.getResource(parentImagesPackageResourceLocation);
        log.info("parentImagesPackageResourceUrl = {}", parentImagesPackageResourceUrl);

        if (parentImagesPackageResourceUrl == null) {
            throw new IllegalStateException("Resource not found : " + parentImagesPackageResourceUrl);
        }

        final File imagesDirectory = new File(parentImagesPackageResourceUrl.getFile());
        log.info("imagesDirectory = {}", imagesDirectory.getAbsolutePath());

        checkImageDirectory(imagesDirectory);
        return imagesDirectory;
    }

    private static boolean checkImageDirectory(File imagesDirectory) {
        if (!imagesDirectory.exists()) {
            log.warn("Directory does not exist : {}", imagesDirectory.getAbsolutePath());
            return false;
        }
        if (!imagesDirectory.isDirectory()) {
            log.warn("File is not a valid Directory : {}", imagesDirectory.getAbsolutePath());
            return false;
        }
        return true;
    }

    public static String getPathSound(final String folder, String language) {

        if (type == CUSTOMIZED) {

            try {

                log.info("CUSTOMIZED");

                String path = new Configuration().whereIsItDir + "sounds/";
                File F = new File(path);

                for (String file : F.list()) {

                    log.info("file " + file);
                    log.info("folder " + folder);

                    if (file.indexOf(folder) >= 0) {

                        File f = new File(path + file);

                        log.info("file " + f.getAbsolutePath());

                        return f.getAbsolutePath();
                    }
                }
            } catch (Exception e) {

                log.info("Problem with customized folder");
                error();
            }

            return "";
        }

        if (language.equals("deu")) {
            // erase when translation is complete
            language = "eng";
        }

        final String voice;
        if (Math.random() > 0.5) {
            voice = "m";
        } else {
            voice = "w";
        }

        return "data/" + getName() + "/sounds/" + language + "/" + folder + "." + voice + "." + language + ".mp3";
    }

    private static String getName() {
        switch (type) {
        case ANIMALNAME:
            return "where-is-the-animal";
        case COLORNAME:
            return "where-is-the-color";
        case CUSTOMIZED:
            return "custumized";
        default:
            log.debug("This case should never happen");
            throw new IllegalStateException("Unsupported type value : " + type);
        }
    }

    @Slf4j
    private static class Pictures extends Group {

        protected static final float zoom_factor = 1.1f;

        private final double minTime;
        private final Group root;
        private final boolean winner;
        private final Rectangle imageRectangle;
        private final double initWidth;
        private final double initHeight;
        private final WhereIsItStats stats;
        private final Scene scene;
        private final ChoiceBox choicebox;

        private Timeline timelineProgressBar;
        private ProgressIndicator indicator;

        private boolean selected;

        private final EventHandler<Event> enterEvent;

        private final Bravo bravo = Bravo.getBravo();

        public Pictures(double posX, double posY, double width, double height, @NonNull Group root,
                @NonNull Scene scene, boolean winner, @NonNull String imagePath, @NonNull ChoiceBox choicebox,
                @NonNull WhereIsItStats stats) {

            log.info("imagePath = {}", imagePath);

            this.minTime = new Configuration().fixationlength;
            this.initWidth = width;
            this.initHeight = height;
            this.selected = false;
            this.winner = winner;
            this.root = root;
            this.stats = stats;
            this.scene = scene;
            this.choicebox = choicebox;
            this.imageRectangle = new Rectangle(posX, posY, width, height);

            this.getChildren().add(imageRectangle);

            final Image image = new Image("file:" + imagePath);

            imageRectangle.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
            indicator = new ProgressIndicator(0);
            indicator.setTranslateX(imageRectangle.getX() + width / 8);
            indicator.setTranslateY(imageRectangle.getY() + height / 8);
            indicator.setMinWidth(width * 0.75);
            indicator.setMinHeight(height * 0.75);
            indicator.setOpacity(0);
            this.getChildren().add(indicator);

            enterEvent = buildEvent();

            GazeUtils.addEventFilter(imageRectangle);

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

                        log.debug("ENTERED");

                        indicator.setOpacity(0.5);
                        indicator.setProgress(0);

                        Timeline timelineCard = new Timeline();

                        timelineProgressBar = new Timeline();

                        timelineProgressBar.getKeyFrames().add(
                                new KeyFrame(new Duration(minTime), new KeyValue(indicator.progressProperty(), 1)));

                        timelineCard.play();

                        timelineProgressBar.play();

                        timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent actionEvent) {

                                log.debug("FINISHED");

                                selected = true;

                                // imageRectangle.removeEventFilter(MouseEvent.ANY, enterEvent);
                                // imageRectangle.removeEventFilter(GazeEvent.ANY, enterEvent);

                                if (winner) {

                                    log.debug("WINNER");

                                    stats.incNbGoals();

                                    int final_zoom = 2;

                                    indicator.setOpacity(0);

                                    Timeline timeline = new Timeline();

                                    // ObservableList<Node> list =
                                    // FXCollections.observableArrayList(root.getChildren());

                                    for (Node N : root.getChildren()) {// clear all but images and reward
                                        // for (Node N : list) {// clear all but images and reward

                                        log.info(N + "");

                                        if ((N instanceof Pictures && imageRectangle != ((Pictures) N).imageRectangle
                                                && !(N instanceof Bravo)) || (N instanceof Home)) {// we put outside
                                            // screen
                                            // Home and cards

                                            log.info(N + " enlevé ");
                                            N.setTranslateX(-10000);
                                            N.setOpacity(0);
                                            // N.removeEventFilter(MouseEvent.ANY, enterEvent);
                                            // N.removeEventFilter(GazeEvent.ANY, enterEvent);
                                        } else {// we keep only Bravo and winning card
                                        }
                                    }

                                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(
                                            imageRectangle.widthProperty(), imageRectangle.getWidth() * final_zoom)));
                                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(
                                            imageRectangle.heightProperty(), imageRectangle.getHeight() * final_zoom)));
                                    timeline.getKeyFrames()
                                            .add(new KeyFrame(new Duration(1000), new KeyValue(
                                                    imageRectangle.xProperty(),
                                                    (scene.getWidth() - imageRectangle.getWidth() * final_zoom) / 2)));
                                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(
                                            imageRectangle.yProperty(),
                                            (scene.getHeight() - imageRectangle.getHeight() * final_zoom) / 2)));

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

                                    disparition.getKeyFrames().add(new KeyFrame(new Duration(2000),
                                            new KeyValue(imageRectangle.opacityProperty(), 0)));

                                    disparition.getKeyFrames()
                                            .add(new KeyFrame(new Duration(2000),
                                                    new KeyValue(imageRectangle.fillProperty(),
                                                            new ImagePattern(new Image("data/common/images/error.png"),
                                                                    0, 0, 1, 1, true))));

                                    apparition.getKeyFrames().add(new KeyFrame(new Duration(1),
                                            new KeyValue(imageRectangle.widthProperty(), initHeight / 2)));

                                    apparition.getKeyFrames().add(new KeyFrame(new Duration(1),
                                            new KeyValue(imageRectangle.heightProperty(), initHeight / 2)));

                                    apparition.getKeyFrames().add(new KeyFrame(new Duration(1),
                                            new KeyValue(imageRectangle.layoutXProperty(), initWidth / 3)));

                                    apparition.getKeyFrames().add(new KeyFrame(new Duration(1),
                                            new KeyValue(imageRectangle.layoutYProperty(), initHeight / 4)));

                                    apparition.getKeyFrames().add(new KeyFrame(new Duration(2000),
                                            new KeyValue(imageRectangle.opacityProperty(), 0.5)));

                                    SequentialTransition sq = new SequentialTransition();
                                    sq.getChildren().addAll(disparition, apparition);
                                    sq.play();

                                    Utils.playSound(WhereIsIt.pathSound);

                                    indicator.setOpacity(0);
                                }
                            }
                        });
                    } else if (e.getEventType() == MouseEvent.MOUSE_EXITED
                            || e.getEventType() == GazeEvent.GAZE_EXITED) {

                        timelineProgressBar.stop();

                        indicator.setOpacity(0);
                        indicator.setProgress(0);
                    }
                }
            };
        }

    }
}
