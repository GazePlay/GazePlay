package net.gazeplay.games.whereisit;

//It is repeated always, it works like a charm :)

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ImageDirectoryLocator;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import static net.gazeplay.games.whereisit.WhereIsIt.WhereIsItGameType.CUSTOMIZED;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt implements GameLifeCycle {

    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;

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

    private Text questionText;

    private final WhereIsItGameType gameType;
    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    private final GameContext gameContext;

    private final Stats stats;

    private RoundDetails currentRoundDetails;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
            final GameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;
    }

    @Override
    public void launch() {

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        Random random = new Random();
        final int winnerImageIndexAmongDisplayedImages = random.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        final Configuration config = Configuration.getInstance();

        currentRoundDetails = pickAndBuildRandomPictures(config, numberOfImagesToDisplayPerRound, random,
                winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {

            Transition animation = createQuestionTransition(currentRoundDetails.question, currentRoundDetails.pictos);
            animation.play();
            if (currentRoundDetails.questionSoundPath != null)
                playQuestionSound();
        }

    }

    private Transition createQuestionTransition(String question, List<Image> Pictos) {

        questionText = new Text(question);

        questionText.setTranslateY(0);

        String color = (Configuration.getInstance().isBackgroundWhite()) ? "titleB" : "titleW";
        questionText.setId(color);

        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double positionX = gamePaneDimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);

        List<Rectangle> pictogramesList = new ArrayList<>(20); // storage of actual Pictogramm nodes in order to delete
        // them
        // from the group later

        if (Pictos != null && !Pictos.isEmpty() && Pictos.size() <= NBMAXPICTO) {

            double screenWidth = Screen.getPrimary().getBounds().getWidth();

            double nbPicto = Pictos.size();

            double pictoSize = screenWidth / (nbPicto + 1);

            log.debug("screenWidth/(nbPicto) : {}", pictoSize);

            pictoSize = Math.min(pictoSize, MAXSIZEPICTO);

            log.debug("Picto Size: {}", pictoSize);

            int i = 0;
            double shift = screenWidth / 2 - ((nbPicto / 2) * pictoSize * 1.1);

            log.debug("shift Size: {}", shift);

            for (Image I : Pictos) {

                Rectangle R = new Rectangle(pictoSize, pictoSize);
                R.setFill(new ImagePattern(I));
                R.setY(positionY + 100);
                R.setX(shift + (i++ * pictoSize * 1.1));
                pictogramesList.add(R);
            }

            gameContext.getChildren().addAll(pictogramesList);
        }

        TranslateTransition fullAnimation = new TranslateTransition(
                Duration.millis(Configuration.getInstance().getQuestionLength() / 2), questionText);
        fullAnimation.setDelay(Duration.millis(Configuration.getInstance().getQuestionLength()));
        double bottomCenter = (0.9 * gamePaneDimension2D.getHeight()) - questionText.getY()
                + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // gameContext.getChildren().remove(questionText);

                gameContext.getChildren().removeAll(pictogramesList);

                log.debug("Adding {} pictures", currentRoundDetails.pictureCardList.size());
                gameContext.getChildren().addAll(currentRoundDetails.pictureCardList);

                for (PictureCard p : currentRoundDetails.pictureCardList) {
                    log.debug("p = {}", p);
                    p.toFront();
                    p.setOpacity(1);
                }

                questionText.toFront();

                stats.notifyNewRoundReady();

                gameContext.onGameStarted();
            }
        });

        return fullAnimation;
    }

    private void playQuestionSound() {

        try {
            log.debug("currentRoundDetails.questionSoundPath: {}", currentRoundDetails.questionSoundPath);

            Utils.playSound(currentRoundDetails.questionSoundPath);
        } catch (Exception e) {
            log.debug("Can't play sound: no associated sound : " + e.toString());
        }
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
        private final List<Image> pictos;
    }

    private RoundDetails pickAndBuildRandomPictures(final Configuration config,
            final int numberOfImagesToDisplayPerRound, final Random random,
            final int winnerImageIndexAmongDisplayedImages) {

        final File imagesDirectory = locateImagesDirectory(config);

        final String language = config.getLanguage();

        final File[] imagesFolders = imagesDirectory.listFiles();

        log.debug("imagesFolders " + imagesFolders);

        final int filesCount = imagesFolders == null ? 0 : imagesFolders.length;

        if (filesCount == 0) {
            log.warn("No image found in Directory " + imagesDirectory);
            error(language);
            return null;
        }

        final int randomFolderIndex = random.nextInt(filesCount);
        log.debug("randomFolderIndex " + randomFolderIndex);

        int step = 1;

        int posX = 0;
        int posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
                .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());
        log.debug("gameSizing = {}", gameSizing);

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

            final int index = (randomFolderIndex + step * i) % filesCount;

            final File folder = imagesFolders[(index) % filesCount];

            log.debug("folder = {}", folder);

            if (!folder.isDirectory())
                continue;

            // final File[] files = folder.listFiles();
            final File[] files = getFiles(folder);

            log.debug("files = {}", (Object[]) files);

            final int numFile = random.nextInt(files.length);

            final File randomImageFile = files[numFile];
            log.debug("randomImageFile = {}", randomImageFile);

            if (winnerImageIndexAmongDisplayedImages == i) {

                log.debug("randomImageFile.getAbsolutePath() " + randomImageFile.getAbsolutePath());

                questionSoundPath = getPathSound(imagesFolders[(index) % filesCount].getName(), language);

                question = getQuestionText(imagesFolders[(index) % filesCount].getName(), language);

                pictograms = getPictogramms(imagesFolders[(index) % filesCount].getName());

                log.debug("pathSound = {}", questionSoundPath);

                log.debug("question = {}", question);
            }

            PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, randomImageFile + "", stats, this);

            pictureCardList.add(pictureCard);

            log.debug("posX " + posX);
            log.debug("posY " + posY);

            if ((i + 1) % nbColumns != 0)
                posX++;
            else {
                posY++;
                posX = 0;
            }
        }

        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question,
                pictograms);
    }

    /**
     * Return all files which don't start with a point
     */
    private File[] getFiles(File folder) {

        File[] files = folder.listFiles();

        int nbElementsToKeep = 0;

        for (File file : files) {
            log.debug("{}", file.getName());
            if (!file.getName().startsWith(".")) {

                nbElementsToKeep++;
            }
        }

        File[] list = new File[nbElementsToKeep];

        int i = 0;

        for (File file : files) {
            log.debug("{}", file.getName());
            if (!file.getName().startsWith(".")) {

                list[i++] = file;
            }
        }

        return list;
    }

    private void error(String language) {

        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        Multilinguism multilinguism = Multilinguism.getSingleton();

        Text error = new Text(multilinguism.getTrad("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. - 100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    private File locateImagesDirectory(Configuration config) {

        File result = null;

        if (this.gameType == CUSTOMIZED) {

            result = new File(config.getWhereIsItDir() + "/images/");
        } else {

            result = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(
                    "data/" + this.gameType.getResourcesDirectoryName() + "/images/");

            if (result == null) {
                result = ImageDirectoryLocator.locateImagesDirectoryInExplodedClassPath(
                        "data/" + this.gameType.getResourcesDirectoryName() + "/images/");
            }
        }
        return result;
    }

    public String getPathSound(final String folder, String language) {

        if (this.gameType == CUSTOMIZED) {

            final Configuration config = Configuration.getInstance();

            try {

                log.debug("CUSTOMIZED");

                String path = config.getWhereIsItDir() + "sounds/";
                File F = new File(path);

                for (String file : F.list()) {

                    log.debug("file " + file);
                    log.debug("folder " + folder);

                    if (file.indexOf(folder) >= 0) {

                        File f = new File(path + file);

                        log.debug("file " + f.getAbsolutePath());

                        return f.getAbsolutePath();
                    }
                }
            } catch (Exception e) {

                log.debug("Problem with customized folder");
                error(config.getLanguage());
            }

            return "";
        }

        if (!(language.equals("fra") || language.equals("eng"))) {
            // erase when translation is complete
            return null;
        }

        log.debug("language is " + language);

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

        log.debug("folder: {}", folder);
        log.debug("language: {}", language);

        if (this.gameType == CUSTOMIZED) {

            final Configuration config = Configuration.getInstance();

            File F = new File(config.getWhereIsItDir() + "questions.csv");

            Multilinguism localMultilinguism = Multilinguism.getForResource(F.toString());

            String traduction = localMultilinguism.getTrad(folder, language);

            return traduction;
        }

        Multilinguism localMultilinguism = Multilinguism.getForResource(gameType.languageResourceLocation);

        String traduction = localMultilinguism.getTrad(folder, language);
        return traduction;
    }

    private List<Image> getPictogramms(final String folder) {

        final String language = "pictos";

        if (this.gameType != CUSTOMIZED) {

            return null;
        }

        final Configuration config = Configuration.getInstance();

        File F = new File(config.getWhereIsItDir() + "questions.csv");

        Multilinguism localMultilinguism = Multilinguism.getForResource(F.toString());

        String traduction = localMultilinguism.getTrad(folder, language);

        log.debug("traduction: {}", traduction);

        StringTokenizer st = new StringTokenizer(traduction, ";");

        String token = null;

        List<Image> L = new ArrayList<>(20);

        while (st.hasMoreTokens()) {

            token = config.getWhereIsItDir() + "pictos/" + st.nextToken().replace('\u00A0', ' ').trim();
            log.debug("token \"{}\"", token);
            File Ftoken = new File(token);
            log.debug("Exists {}", Ftoken.exists());
            if (Ftoken.exists()) {

                L.add(new Image(Ftoken.toURI().toString()));
            }
        }

        log.debug("L {}", L);
        return L;
    }

    @Slf4j
    @ToString
    private static class PictureCard extends Group {

        private final double minTime;
        private final GameContext gameContext;
        private final boolean winner;

        private final ImageView imageRectangle;
        private final Rectangle errorImageRectangle;

        private final double initialWidth;
        private final double initialHeight;

        private final double initialPositionX;
        private final double initialPositionY;

        private final Stats stats;
        private final String imagePath;

        private final ProgressIndicator progressIndicator;
        private final Timeline progressIndicatorAnimationTimeLine;

        private boolean selected;

        private final CustomInputEventHandler customInputEventHandler;

        private final WhereIsIt gameInstance;

        public PictureCard(double posX, double posY, double width, double height, @NonNull GameContext gameContext,
                boolean winner, @NonNull String imagePath, @NonNull Stats stats, WhereIsIt gameInstance) {

            log.info("imagePath = {}", imagePath);

            final Configuration config = Configuration.getInstance();

            this.minTime = config.getFixationLength();
            this.initialPositionX = posX;
            this.initialPositionY = posY;
            this.initialWidth = width;
            this.initialHeight = height;
            this.selected = false;
            this.winner = winner;
            this.gameContext = gameContext;
            this.stats = stats;
            this.gameInstance = gameInstance;

            this.imagePath = imagePath;

            this.imageRectangle = createImageView(posX, posY, width, height, imagePath);
            this.progressIndicator = buildProgressIndicator(width, height);

            this.progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);

            this.errorImageRectangle = createErrorImageRectangle();

            this.getChildren().add(imageRectangle);
            this.getChildren().add(progressIndicator);
            this.getChildren().add(errorImageRectangle);

            customInputEventHandler = buildCustomInputEventHandler(gameInstance);

            gameContext.getGazeDeviceManager().addEventFilter(imageRectangle);

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
                    gameContext.getGazeDeviceManager().removeEventFilter(imageRectangle);

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

            this.toFront();

            Dimension2D gamePanelDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            log.info("gamePanelDimension2D = {}", gamePanelDimension2D);

            ScaleTransition scaleToFullScreenTransition = new ScaleTransition(new Duration(1000), imageRectangle);
            scaleToFullScreenTransition.setByX((gamePanelDimension2D.getWidth() / initialWidth) - 1);
            scaleToFullScreenTransition.setByY((gamePanelDimension2D.getHeight() / initialHeight) - 1);

            TranslateTransition translateToCenterTransition = new TranslateTransition(new Duration(1000),
                    imageRectangle);
            translateToCenterTransition
                    .setByX(-initialPositionX + (gamePanelDimension2D.getWidth() - initialWidth) / 2);
            translateToCenterTransition
                    .setByY(-initialPositionY + (gamePanelDimension2D.getHeight() - initialHeight) / 2);

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

        private ImageView createImageView(double posX, double posY, double width, double height,
                @NonNull String imagePath) {
            final Image image = new Image("file:" + imagePath);

            ImageView result = new ImageView(image);
            result.setX(posX);
            result.setY(posY);
            result.setFitWidth(width);
            result.setFitHeight(height);
            result.setPreserveRatio(false);
            return result;
        }

        private Rectangle createErrorImageRectangle() {
            final Image image = new Image("data/common/images/error.png");

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double imageHeightToWidthRatio = imageHeight / imageWidth;

            double rectangleWidth = imageRectangle.getFitWidth() / 3;
            double rectangleHeight = imageHeightToWidthRatio * rectangleWidth;

            double positionX = imageRectangle.getX() + (imageRectangle.getFitWidth() - rectangleWidth) / 2;
            double positionY = imageRectangle.getY() + (imageRectangle.getFitHeight() - rectangleHeight) / 2;

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
