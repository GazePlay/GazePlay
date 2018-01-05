package net.gazeplay.games.whereisit;

//It is repeated always, it works like a charm :)

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
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
public class WhereIsIt implements GameLifeCycle {

    public enum WhereIsItGameType {
        ANIMALNAME("where-is-the-animal", "where-is-the-animal"), COLORNAME("where-is-the-color",
                "where-is-the-color"), CUSTOMIZED("custumized", "custumized");

        @Getter
        private final String gameName;

        @Getter
        private final String resourcesDirectoryName;

        @Getter
        private final String languageResourceLocation;

        WhereIsItGameType(String gameName, String resourcesDirectoryName) {
            this.gameName = gameName;
            this.resourcesDirectoryName = resourcesDirectoryName;
            this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        }
    }

    private final WhereIsItGameType gameType;
    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    private final GameContext gameContext;
    private final Scene scene;

    private final WhereIsItStats stats;

    private RoundDetails currentRoundDetails;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
            final GameContext gameContext, final WhereIsItStats stats) {
        this.gameContext = gameContext;
        this.scene = gameContext.getScene();
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;

        this.stats.setName(gameType.getGameName());
    }

    @Override
    public void launch() {
        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree).computeGameSizing(scene);

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        Random random = new Random();
        final int winnerImageIndexAmongDisplayedImages = random.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        currentRoundDetails = pickAndBuildRandomPictures(config, gameSizing, numberOfImagesToDisplayPerRound, random,
                winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {
            Transition animation = createQuestionTextTransition(currentRoundDetails.question);

            animation.play();
            playQuestionSound();
        }
    }

    private Transition createQuestionTextTransition(String question) {

        Text questionText = new Text(question);

        questionText.setId("title");

        double positionX = gameContext.getScene().getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        double positionY = gameContext.getScene().getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().addAll(questionText);

        TranslateTransition fullAnimation = new TranslateTransition(Duration.millis(2000), questionText);
        fullAnimation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                gameContext.getChildren().remove(questionText);

                gameContext.getChildren().addAll(currentRoundDetails.pictureCardList);
                stats.start();

                gameContext.onGameStarted();
            }
        });

        return fullAnimation;
    }

    private void playQuestionSound() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL soundResourceUrl = classLoader.getResource(currentRoundDetails.questionSoundPath);
        AudioClip soundClip;

        log.info("currentRoundDetails.questionSoundPath: {}", currentRoundDetails.questionSoundPath);

        if (soundResourceUrl == null)
            soundClip = new AudioClip("file:" + currentRoundDetails.questionSoundPath);
        else
            soundClip = new AudioClip(soundResourceUrl.toExternalForm());
        soundClip.play();
    }

    /**
     * this method should be called when exiting the game, or before starting a new round, in order to clean up all
     * resources in both UI and memory
     */
    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.pictureCardList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.pictureCardList);
            }
            currentRoundDetails = null;
        }
    }

    public void removeAllIncorrectPictureCards() {
        if (this.currentRoundDetails == null) {
            return;
        }

        // Collect all items to be removed from the User Interface
        List<PictureCard> pictureCardsToHide = new ArrayList<>();
        for (PictureCard pictureCard : this.currentRoundDetails.pictureCardList) {
            if (!pictureCard.winner) {
                pictureCardsToHide.add(pictureCard);
            }
        }

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(pictureCardsToHide);
    }

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        private final List<PictureCard> pictureCardList;
        private final int winnerImageIndexAmongDisplayedImages;
        private final String questionSoundPath;
        private final String question;
    }

    private RoundDetails pickAndBuildRandomPictures(final Configuration config, final GameSizing gameSizing,
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
        String questionSoundPath = null;
        String question = null;

        for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

            final int index = (randomFolderIndex + step * i) % filesCount;

            final File[] files = imagesFolders[(index) % filesCount].listFiles();

            final int numFile = random.nextInt(files.length);

            final File randomImageFile = files[numFile];
            log.info("randomImageFile = {}", randomImageFile);

            if (winnerImageIndexAmongDisplayedImages == i) {

                log.info("randomImageFile.getAbsolutePath() " + randomImageFile.getAbsolutePath());

                questionSoundPath = getPathSound(imagesFolders[(index) % filesCount].getName(), language);

                question = getQuestionText(imagesFolders[(index) % filesCount].getName(), language);

                log.info("pathSound = {}", questionSoundPath);

                log.info("question = {}", question);
            }

            PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
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

        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question);
    }

    private void error(String language) {

        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        Multilinguism multilinguism = Multilinguism.getSingleton();

        Text error = new Text(multilinguism.getTrad("WII-error", language));
        error.setX(scene.getWidth() / 2. - 100);
        error.setY(scene.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
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

    private String getQuestionText(final String folder, String language) {

        log.info("folder: {}", folder);
        log.info("language: {}", language);

        if (this.gameType == CUSTOMIZED) {

            final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

            File F = new File(config.getWhereIsItDir() + "questions.csv");

            log.info("F: {}", F.toString());

            Multilinguism localMultilinguism = Multilinguism.getForResource(F.toString());

            String traduction = localMultilinguism.getTrad(folder, language);

            return traduction;
        }

        if (language.equals("deu")) {
            // erase when translation is complete
            language = "eng";
        }

        Multilinguism localMultilinguism = Multilinguism.getForResource(gameType.languageResourceLocation);

        String traduction = localMultilinguism.getTrad(folder, language);
        return traduction;
    }

    @Slf4j
    private static class PictureCard extends Group {

        private final double minTime;
        private final GameContext gameContext;
        private final boolean winner;

        private final Rectangle imageRectangle;
        private final Rectangle errorImageRectangle;

        private final double initialWidth;
        private final double initialHeight;

        private final double initialPositionX;
        private final double initialPositionY;

        private final WhereIsItStats stats;
        private final Scene scene;
        private final String imagePath;

        private final ProgressIndicator progressIndicator;
        private final Timeline progressIndicatorAnimationTimeLine;

        private boolean selected;

        private final CustomInputEventHandler customInputEventHandler;

        private final WhereIsIt gameInstance;

        public PictureCard(double posX, double posY, double width, double height, @NonNull GameContext gameContext,
                boolean winner, @NonNull String imagePath, @NonNull WhereIsItStats stats, WhereIsIt gameInstance) {

            log.info("imagePath = {}", imagePath);

            final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

            this.minTime = config.getFixationlength();
            this.initialPositionX = posX;
            this.initialPositionY = posY;
            this.initialWidth = width;
            this.initialHeight = height;
            this.selected = false;
            this.winner = winner;
            this.gameContext = gameContext;
            this.stats = stats;
            this.scene = gameContext.getScene();
            this.gameInstance = gameInstance;

            this.imagePath = imagePath;

            this.imageRectangle = createImageRectangle(posX, posY, width, height, imagePath);
            this.progressIndicator = buildProgressIndicator(width, height);

            this.progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);

            this.errorImageRectangle = createErrorImageRectangle();

            this.getChildren().add(imageRectangle);
            this.getChildren().add(progressIndicator);
            this.getChildren().add(errorImageRectangle);

            customInputEventHandler = buildCustomInputEventHandler(gameInstance);

            GazeUtils.addEventFilter(imageRectangle);

            this.addEventFilter(MouseEvent.ANY, customInputEventHandler);

            this.addEventFilter(GazeEvent.ANY, customInputEventHandler);
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

                    imageRectangle.removeEventFilter(MouseEvent.ANY, customInputEventHandler);
                    imageRectangle.removeEventFilter(GazeEvent.ANY, customInputEventHandler);
                    GazeUtils.removeEventFilter(imageRectangle);

                    if (winner) {
                        onCorrectCardSelected(gameInstance);
                    } else {
                        // bad card
                        onWrongCardSelected(gameInstance);
                    }
                }
            };
        }

        private void onCorrectCardSelected(WhereIsIt gameInstance) {
            log.debug("WINNER");

            stats.incNbGoals();

            customInputEventHandler.ignoreAnyInput = true;
            progressIndicator.setVisible(false);

            gameInstance.removeAllIncorrectPictureCards();

            Rectangle2D sceneBounds = new Rectangle2D(scene.getX(), scene.getY(), scene.getWidth(), scene.getHeight());
            log.info("sceneBounds = {}", sceneBounds);

            ScaleTransition scaleToFullScreenTransition = new ScaleTransition(new Duration(1000), imageRectangle);
            scaleToFullScreenTransition.setByX((sceneBounds.getWidth() / initialWidth) - 1);
            scaleToFullScreenTransition.setByY((sceneBounds.getHeight() / initialHeight) - 1);

            TranslateTransition translateToCenterTransition = new TranslateTransition(new Duration(1000),
                    imageRectangle);
            translateToCenterTransition.setByX(-initialPositionX + (sceneBounds.getWidth() - initialWidth) / 2);
            translateToCenterTransition.setByY(-initialPositionY + (sceneBounds.getHeight() - initialHeight) / 2);

            ParallelTransition fullAnimation = new ParallelTransition();
            fullAnimation.getChildren().add(translateToCenterTransition);
            fullAnimation.getChildren().add(scaleToFullScreenTransition);

            fullAnimation.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            gameInstance.dispose();
                            gameContext.clear();

                            gameInstance.launch();
                            // HomeUtils.home(gameInstance.scene, gameInstance.group, gameInstance.choiceBox,
                            // gameInstance.stats);

                            gameContext.onGameStarted();

                        }
                    });
                }
            });

            fullAnimation.play();
        }

        private void onWrongCardSelected(WhereIsIt gameInstance) {
            customInputEventHandler.ignoreAnyInput = true;
            progressIndicator.setVisible(false);

            FadeTransition imageFadeOutTransition = new FadeTransition(new Duration(1500), imageRectangle);
            imageFadeOutTransition.setFromValue(1);
            // the final opacity is not zero so that we can see what was the image, even after it is marked as an
            // erroneous pick
            imageFadeOutTransition.setToValue(0.2);

            errorImageRectangle.toFront();
            errorImageRectangle.setOpacity(0);
            errorImageRectangle.setVisible(true);

            FadeTransition errorFadeInTransition = new FadeTransition(new Duration(650), errorImageRectangle);
            errorFadeInTransition.setFromValue(0);
            errorFadeInTransition.setToValue(1);

            ParallelTransition fullAnimation = new ParallelTransition();
            fullAnimation.getChildren().addAll(imageFadeOutTransition, errorFadeInTransition);

            fullAnimation.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    gameInstance.playQuestionSound();
                    customInputEventHandler.ignoreAnyInput = false;
                }
            });

            fullAnimation.play();
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

        private ProgressIndicator buildProgressIndicator(double parentWidth, double parentHeight) {
            double minWidth = parentWidth / 2;
            double minHeight = parentHeight / 2;

            double positionX = imageRectangle.getX() + (parentWidth - minWidth) / 2;
            double positionY = imageRectangle.getY() + (parentHeight - minHeight) / 2;

            ProgressIndicator result = new ProgressIndicator(0);
            result.setTranslateX(positionX);
            result.setTranslateY(positionY);
            result.setMinWidth(minWidth);
            result.setMinHeight(minHeight);
            result.setOpacity(0.5);
            result.setVisible(false);
            return result;
        }

        private CustomInputEventHandler buildCustomInputEventHandler(final WhereIsIt gameInstance) {
            return new CustomInputEventHandler();
        }

        private class CustomInputEventHandler implements EventHandler<Event> {

            /**
             * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
             * do not want the game to continue to process input, as the user input is irrelevant while the animation is
             * in progress
             */
            private boolean ignoreAnyInput = false;

            @Override
            public void handle(Event e) {
                if (ignoreAnyInput) {
                    return;
                }

                if (selected) {
                    return;
                }

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    onEntered();
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    onExited();
                }

            }

            private void onEntered() {
                log.info("ENTERED {}", imagePath);

                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);

                progressIndicatorAnimationTimeLine.playFromStart();
            }

            private void onExited() {
                log.info("EXITED {}", imagePath);

                progressIndicatorAnimationTimeLine.stop();

                progressIndicator.setVisible(false);
                progressIndicator.setProgress(0);
            }

        }

    }
}
