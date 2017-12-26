package net.gazeplay.games.whereisit;

//It is repeated always, it works like a charm :)

import com.sun.glass.ui.Screen;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.Home;
import net.gazeplay.commons.utils.HomeUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.gazeplay.games.whereisit.WhereIsIt.WhereIsItGameType.CUSTOMIZED;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt {

    public enum WhereIsItGameType {
        ANIMALNAME("where-is-the-animal", "where-is-the-animal"), COLORNAME("where-is-the-color",
                "where-is-the-color"), CUSTOMIZED("custumized", "custumized");

        @Getter
        private final String gameName;

        @Getter
        private final String resourcesDirectoryName;

        WhereIsItGameType(String gameName, String resourcesDirectoryName) {
            this.gameName = gameName;
            this.resourcesDirectoryName = resourcesDirectoryName;
        }
    }

    private final WhereIsItGameType gameType;
    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    private final Group group;
    private final Scene scene;
    private final ChoiceBox choiceBox;

    private String pathSound;

    private final WhereIsItStats stats;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
            final Group group, final Scene scene, final ChoiceBox choiceBox, final WhereIsItStats stats) {
        this.group = group;
        this.scene = scene;
        this.choiceBox = choiceBox;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;

        this.stats.setName(gameType.getGameName());
    }

    public void buildGame() {
        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree).computeGameSizing();

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        Random random = new Random();
        final int winnerImageIndexAmongDisplayedImages = random.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        List<PictureCard> pictureCardList = pickAndBuildRandomPictures(config, gameSizing,
                numberOfImagesToDisplayPerRound, random, winnerImageIndexAmongDisplayedImages);

        if (pictureCardList != null) {
            group.getChildren().addAll(pictureCardList);
            stats.start();
        }
    }

    private List<PictureCard> pickAndBuildRandomPictures(final Configuration config, final GameSizing gameSizing,
            final int numberOfImagesToDisplayPerRound, final Random random,
            final int winnerImageIndexAmongDisplayedImages) {

        final File imagesDirectory = locateImagesDirectory(config);
        final String language = config.getLanguage();

        final File[] imagesFolders = imagesDirectory.listFiles();
        final int filesCount = imagesFolders == null ? 0 : imagesFolders.length;

        if (filesCount == 0) {
            log.warn("No image found in Directory " + imagesDirectory);
            error(language);
            return null;
        }

        final int randomFolderIndex = random.nextInt(filesCount);
        log.info("randomFolderIndex " + randomFolderIndex);

        int step = 1; // (int) (Math.random() + 1.5);
        log.info("step " + step);

        int posX = 0;
        int posY = 0;

        final List<PictureCard> pictureCardList = new ArrayList<>();

        for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

            final int index = (randomFolderIndex + step * i) % filesCount;

            final File[] files = imagesFolders[(index) % filesCount].listFiles();

            final int numFile = random.nextInt(files.length);

            final File randomImageFile = files[numFile];
            log.info("randomImageFile = {}", randomImageFile);

            if (winnerImageIndexAmongDisplayedImages == i) {

                log.info("randomImageFile.getAbsolutePath() " + randomImageFile.getAbsolutePath());

                this.pathSound = getPathSound(imagesFolders[(index) % filesCount].getName(), language);
                log.info("pathSound = {}", this.pathSound);
                Utils.playSound(this.pathSound);
            }

            PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, group, scene,
                    winnerImageIndexAmongDisplayedImages == i, randomImageFile + "", stats, this);

            pictureCardList.add(pictureCard);

            log.info("posX " + posX);
            log.info("posY " + posY);

            if ((i + 1) % nbColumns != 0)
                posX++;
            else {
                posY++;
                posX = 0;
            }
        }

        return pictureCardList;
    }

    private void error(String language) {

        HomeUtils.clear(scene, group, choiceBox);
        HomeUtils.home(scene, group, choiceBox, null);

        Multilinguism multilinguism = Multilinguism.getSingleton();
        Text error = new Text(multilinguism.getTrad("WII-error", language));
        error.setX(Screen.getMainScreen().getWidth() / 2. - 100);
        error.setY(Screen.getMainScreen().getHeight() / 2.);
        error.setId("item");
        group.getChildren().addAll(error);
    }

    private File locateImagesDirectory(Configuration config) {

        File result = null;

        if (this.gameType == CUSTOMIZED) {

            result = new File(config.getWhereIsItDir() + "/images/");
        } else {

            result = locateImagesDirectoryInUnpackedDistDirectory();

            if (result == null) {
                result = locateImagesDirectoryInExplodedClassPath();
            }
        }
        return result;
    }

    private File locateImagesDirectoryInUnpackedDistDirectory() {
        final File workingDirectory = new File(".");
        log.info("workingDirectory = {}", workingDirectory.getAbsolutePath());
        final String workingDirectoryName;
        try {
            workingDirectoryName = workingDirectory.getCanonicalFile().getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("workingDirectoryName = {}", workingDirectoryName);

        final String parentImagesPackageResourceLocation = "data/" + this.gameType.getResourcesDirectoryName()
                + "/images/";
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

    private File locateImagesDirectoryInExplodedClassPath() {
        final String parentImagesPackageResourceLocation = "data/" + this.gameType.getResourcesDirectoryName()
                + "/images/";
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

    public String getPathSound(final String folder, String language) {

        if (this.gameType == CUSTOMIZED) {

            final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

            try {

                log.info("CUSTOMIZED");

                String path = config.getWhereIsItDir() + "sounds/";
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
                error(config.getLanguage());
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

        return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + language + "/" + folder + "." + voice
                + "." + language + ".mp3";
    }

    @Slf4j
    private static class PictureCard extends Group {

        protected static final float zoom_factor = 1.1f;

        private final double minTime;
        private final Group root;
        private final boolean winner;

        private final Rectangle imageRectangle;
        private final Rectangle errorImageRectangle;

        private final double initWidth;
        private final double initHeight;
        private final WhereIsItStats stats;
        private final Scene scene;
        private final String imagePath;

        private final ProgressIndicator progressIndicator;
        private final Timeline progressIndicatorAnimationTimeLine;

        private boolean selected;

        private final Bravo bravo = Bravo.getBravo();

        public PictureCard(double posX, double posY, double width, double height, @NonNull Group root,
                @NonNull Scene scene, boolean winner, @NonNull String imagePath, @NonNull WhereIsItStats stats,
                WhereIsIt gameInstance) {

            log.info("imagePath = {}", imagePath);

            final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

            this.minTime = config.getFixationlength();
            this.initWidth = width;
            this.initHeight = height;
            this.selected = false;
            this.winner = winner;
            this.root = root;
            this.stats = stats;
            this.scene = scene;

            this.imagePath = imagePath;

            this.imageRectangle = createImageRectangle(posX, posY, width, height, imagePath);
            this.progressIndicator = buildProgressIndicator(width, height);

            this.progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);

            this.errorImageRectangle = createErrorImageRectangle();

            this.getChildren().add(imageRectangle);
            this.getChildren().add(progressIndicator);
            this.getChildren().add(errorImageRectangle);

            EventHandler<Event> enterEvent = buildEvent(gameInstance);

            GazeUtils.addEventFilter(imageRectangle);

            this.addEventFilter(MouseEvent.ANY, enterEvent);

            this.addEventFilter(GazeEvent.ANY, enterEvent);
        }

        private Timeline createProgressIndicatorTimeLine(WhereIsIt gameInstance) {
            Timeline result = new Timeline();

            result.getKeyFrames()
                    .add(new KeyFrame(new Duration(minTime), new KeyValue(progressIndicator.progressProperty(), 1)));

            EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
                    gameInstance);

            result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

            return result;
        }

        private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(WhereIsIt gameInstance) {
            return new EventHandler<ActionEvent>() {

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

                        progressIndicator.setVisible(false);

                        // ObservableList<Node> list =
                        // FXCollections.observableArrayList(root.getChildren());

                        for (Node N : root.getChildren()) {// clear all but images and reward
                            // for (Node N : list) {// clear all but images and reward

                            log.info(N + "");

                            if ((N instanceof PictureCard && imageRectangle != ((PictureCard) N).imageRectangle
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

                        Timeline timeline = new Timeline();

                        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                new KeyValue(imageRectangle.widthProperty(), imageRectangle.getWidth() * final_zoom)));
                        timeline.getKeyFrames()
                                .add(new KeyFrame(new Duration(1000), new KeyValue(imageRectangle.heightProperty(),
                                        imageRectangle.getHeight() * final_zoom)));
                        timeline.getKeyFrames()
                                .add(new KeyFrame(new Duration(1000), new KeyValue(imageRectangle.xProperty(),
                                        (scene.getWidth() - imageRectangle.getWidth() * final_zoom) / 2)));
                        timeline.getKeyFrames()
                                .add(new KeyFrame(new Duration(1000), new KeyValue(imageRectangle.yProperty(),
                                        (scene.getHeight() - imageRectangle.getHeight() * final_zoom) / 2)));

                        timeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {

                                bravo.playWinTransition(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {

                                        HomeUtils.clear(gameInstance.scene, gameInstance.group, gameInstance.choiceBox);
                                        gameInstance.buildGame();
                                        HomeUtils.home(gameInstance.scene, gameInstance.group, gameInstance.choiceBox,
                                                gameInstance.stats);

                                    }
                                });
                            }
                        });

                        timeline.play();

                    } else {// bad card

                        progressIndicator.setVisible(false);

                        FadeTransition imageFadeOutTransition = new FadeTransition(new Duration(2000), imageRectangle);
                        imageFadeOutTransition.setFromValue(1);
                        imageFadeOutTransition.setToValue(0);

                        errorImageRectangle.toFront();
                        errorImageRectangle.setOpacity(0);
                        errorImageRectangle.setVisible(true);

                        FadeTransition errorFadeInTransition = new FadeTransition(new Duration(500),
                                errorImageRectangle);
                        errorFadeInTransition.setFromValue(0);
                        errorFadeInTransition.setToValue(1);

                        ParallelTransition fullAnimation = new ParallelTransition();
                        fullAnimation.getChildren().addAll(imageFadeOutTransition, errorFadeInTransition);

                        fullAnimation.setOnFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Utils.playSound(gameInstance.pathSound);
                            }
                        });

                        fullAnimation.play();
                    }
                }
            };
        }

        private Rectangle createImageRectangle(double posX, double posY, double width, double height,
                @NonNull String imagePath) {
            final Image image = new Image("file:" + imagePath);

            Rectangle result = new Rectangle(posX, posY, width, height);
            result.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
            return result;
        }

        private Rectangle createErrorImageRectangle() {
            final Image image = new Image("data/common/images/error.png");

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double imageHeightToWidthRatio = imageHeight / imageWidth;

            double rectangleWidth = imageRectangle.getWidth() / 3;
            double rectangleHeight = imageHeightToWidthRatio * rectangleWidth;

            double positionX = imageRectangle.getX() + (imageRectangle.getWidth() - rectangleWidth) / 2;
            double positionY = imageRectangle.getY() + (imageRectangle.getHeight() - rectangleHeight) / 2;

            Rectangle errorImageRectangle = new Rectangle(rectangleWidth, rectangleHeight);
            errorImageRectangle.setFill(new ImagePattern(image));
            errorImageRectangle.setX(positionX);
            errorImageRectangle.setY(positionY);
            errorImageRectangle.setOpacity(0);
            errorImageRectangle.setVisible(false);
            return errorImageRectangle;
        }

        private ProgressIndicator buildProgressIndicator(double width, double height) {
            ProgressIndicator result = new ProgressIndicator(0);
            result.setTranslateX(imageRectangle.getX() + width / 8);
            result.setTranslateY(imageRectangle.getY() + height / 8);
            result.setMinWidth(width * 0.75);
            result.setMinHeight(height * 0.75);
            result.setOpacity(0.5);
            result.setVisible(false);
            return result;
        }

        private EventHandler<Event> buildEvent(final WhereIsIt gameInstance) {

            return new EventHandler<Event>() {
                @Override
                public void handle(Event e) {

                    if (selected) {
                        return;
                    }

                    if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                        log.info("ENTERED {}", imagePath);

                        progressIndicator.setProgress(0);
                        progressIndicator.setVisible(true);

                        progressIndicatorAnimationTimeLine.playFromStart();

                    } else if (e.getEventType() == MouseEvent.MOUSE_EXITED
                            || e.getEventType() == GazeEvent.GAZE_EXITED) {

                        log.info("EXITED {}", imagePath);

                        progressIndicatorAnimationTimeLine.stop();

                        progressIndicator.setVisible(false);
                        progressIndicator.setProgress(0);
                    }
                }
            };
        }

    }
}
