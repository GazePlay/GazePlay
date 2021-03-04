package net.gazeplay.games.whereisitconfigurable;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.WhereIsItValidator;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.gazeplay.commons.utils.games.WhereIsItValidator.getValidSoundFiles;

@Slf4j
@ToString
@Getter
class PictureCard extends Group {

    private final double minTime;
    private final IGameContext gameContext;
    private final boolean winner;

    @Getter
    private ImageView imageRectangle;
    private ImageView errorImageRectangle;

    private final double initialWidth;
    private final double initialHeight;

    private final double initialPositionX;
    private final double initialPositionY;

    private final Stats stats;
    private final String imagePath;

    private final ProgressIndicator progressIndicator;
    private final Timeline progressIndicatorAnimationTimeLine;

    private boolean selected;

    @Getter
    private final PictureCard.CustomInputEventHandler customInputEventHandler;

    private final WhereIsItConfigurable gameInstance;
    private final Configuration config;

    private final File folder;

    PictureCard(double posX, double posY, double width, double height, @NonNull IGameContext gameContext,
                boolean winner, File folder, @NonNull String imagePath, @NonNull Stats stats, WhereIsItConfigurable gameInstance) {

        log.info("imagePath = {}", imagePath);

        this.config = gameContext.getConfiguration();

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
        this.folder = folder;

        this.imagePath = imagePath;


        this.imageRectangle = createImageView(posX, posY, width, height, imagePath);

        this.progressIndicator = buildProgressIndicator(width, height);

        this.progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);

        this.errorImageRectangle = createErrorImageRectangle(posX, posY, width, height);

        this.getChildren().add(imageRectangle);
        this.getChildren().add(progressIndicator);
        this.getChildren().add(errorImageRectangle);

        customInputEventHandler = new PictureCard.CustomInputEventHandler();

        gameContext.getGazeDeviceManager().addEventFilter(imageRectangle);
        this.addEventFilter(MouseEvent.ANY, customInputEventHandler);
        this.addEventFilter(GazeEvent.ANY, customInputEventHandler);
    }

    private Timeline createProgressIndicatorTimeLine(WhereIsItConfigurable gameInstance) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(minTime), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            gameInstance);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(WhereIsItConfigurable gameInstance) {
        return actionEvent -> {

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
        };
    }

    private void onCorrectCardSelected(WhereIsItConfigurable gameInstance) {
        log.debug("WINNER");

        stats.incrementNumberOfGoalsReached();

        customInputEventHandler.ignoreAnyInput = true;
        progressIndicator.setVisible(false);

        gameInstance.removeAllIncorrectPictureCards();

        this.toFront();

        Dimension2D gamePanelDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("gamePanelDimension2D = {}", gamePanelDimension2D);

        gameContext.updateScore(stats, gameInstance);

        EventHandler<ActionEvent> action = actionEvent1 -> {
            gameInstance.questionIndex++;
            if(gameInstance.questionIndex >= gameInstance.getNumberOfQuestions()){
                gameInstance.questionIndex = 0;
                gameInstance.dispose();
                gameContext.clear();
                gameContext.showRoundStats(stats,gameInstance);
            }else {
                gameInstance.dispose();
                gameContext.clear();
                try {
                    stats.saveStats();
                    stats.reset();
                    stats.incrementNumberOfGoalsToReach();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gameInstance.launch();
                gameContext.onGameStarted();
            }

        };

        File soundFile = new File(config.getWhereIsItConfigurableDir() + "/common/win/sounds/" + gameInstance.getCurrentQuestionAsnwer().soundBravo);
        File imageFile = new File(config.getWhereIsItConfigurableDir() + "/common/win/images/" + gameInstance.getCurrentQuestionAsnwer().imageBravo);
        File videoFile = new File(config.getWhereIsItConfigurableDir() + "/common/win/videos/" + gameInstance.getCurrentQuestionAsnwer().videoBravo);


        File parent = new File(config.getWhereIsItConfigurableDir());
        getChildAndBom(parent,"");


        log.info(" vid = {} && {} && {}", gameInstance.getCurrentQuestionAsnwer().videoBravo, videoFile,videoFile.exists());
        log.info(" image = {} && {} && {}", gameInstance.getCurrentQuestionAsnwer().imageBravo, imageFile,imageFile.exists());
        log.info(" sound = {} && {} && {}", gameInstance.getCurrentQuestionAsnwer().soundBravo, soundFile,soundFile.exists());
        if (gameInstance.getCurrentQuestionAsnwer().videoBravo != null && videoFile.exists()) {
            //fullAnimation.setOnFinished(actionEvent ->
            log.info("**** BRAVO VIDEO");
            gameContext.playWinTransition(
                500,
                videoFile.getAbsolutePath(),
                action
                //        )
            );
        } else if (
            gameInstance.getCurrentQuestionAsnwer().soundBravo != null && soundFile.exists() &&
                gameInstance.getCurrentQuestionAsnwer().imageBravo != null && imageFile.exists()) {
            log.info("**** IMAGE AND SOUND BRAVO");
            // fullAnimation.setOnFinished(actionEvent ->
            gameContext.playWinTransition(
                500,
                imageFile.getAbsolutePath(),
                soundFile.getAbsolutePath(),
                action
                // )
            );
        } else {
            log.info("**** BASIC BRAVO");
            // fullAnimation.setOnFinished(actionEvent ->
            gameContext.playWinTransition(
                500,
                action
                // )
            );
        }
        // fullAnimation.play();
    }

    public void getChildAndBom(File parent, String offset){
        File[] children = parent.listFiles(file ->
            !file.getName().startsWith(".")
        );

        if(children!=null) {
            for (File child : children) {
                if (child.getName().contains("\uFEFF")) {
                    log.info(offset + "** {}", child.getName());
                } else {
                    log.info(offset +"{}", child.getName());
                }
                if(child.isDirectory())
                    getChildAndBom(child,offset+"\t");
            }
        }
    }

    private void onWrongCardSelected(WhereIsItConfigurable gameInstance) {
        customInputEventHandler.ignoreAnyInput = true;
        progressIndicator.setVisible(false);

        FadeTransition imageFadeOutTransition = new FadeTransition(new Duration(1500), imageRectangle);
        imageFadeOutTransition.setFromValue(1);
        // the final opacity is not zero so that we can see what was the image, even after it is marked as an
        // erroneous pick
        imageFadeOutTransition.setToValue(0);

        errorImageRectangle.toFront();
        errorImageRectangle.setOpacity(0);
        errorImageRectangle.setVisible(true);

        FadeTransition errorFadeInTransition = new FadeTransition(new Duration(650), errorImageRectangle);
        errorFadeInTransition.setFromValue(0);
        errorFadeInTransition.setToValue(1);

        ParallelTransition fullAnimation = new ParallelTransition();
        fullAnimation.getChildren().addAll(imageFadeOutTransition, errorFadeInTransition);

        fullAnimation.setOnFinished(actionEvent -> {
            File soundResource = new File(folder, "/fail/sounds");
            if (!soundResource.exists()) {
                soundResource = new File(folder.getParentFile().getParentFile(), "/common/fail/sounds");
            }
            File[] files = WhereIsItConfigurable.getFiles(soundResource);
            if(files !=null) {
                List<File> validSoundFiles = getValidSoundFiles(files);
            if (validSoundFiles.size() == 0) {
                soundResource = new File(folder.getParentFile().getParentFile(), "/common/fail/sounds");
                files = WhereIsItConfigurable.getFiles(soundResource);
                validSoundFiles = getValidSoundFiles(files);
            }

            final File randomImageFile = validSoundFiles.get(0);
            gameContext.getSoundManager().add(randomImageFile.getAbsolutePath());
            }
            log.info("WERE CHOOSING THE FILE {}", soundResource);

            customInputEventHandler.ignoreAnyInput = false;
        });

        fullAnimation.play();
    }

    private ImageView createImageView(double posX, double posY, double width, double height,
                                      @NonNull String imagePath) {
        final Image image = new Image(imagePath );

        ImageView result = new ImageView(image);

        result.setFitWidth(width);
        result.setFitHeight(height);

        double ratioX = result.getFitWidth() / image.getWidth();
        double ratioY = result.getFitHeight() / image.getHeight();

        double reducCoeff = Math.min(ratioX, ratioY);

        double w = image.getWidth() * reducCoeff;
        double h = image.getHeight() * reducCoeff;

        result.setX(posX);
        result.setY(posY);
        result.setTranslateX((result.getFitWidth() - w) / 2);
        result.setTranslateY((result.getFitHeight() - h) / 2);
        result.setPreserveRatio(true);

        return result;
    }

    private ImageView createStretchedImageView(double posX, double posY, double width, double height,
                                               @NonNull String imagePath) {
        final Image image = new Image(imagePath);

        ImageView result = new ImageView(image);

        result.setFitWidth(width);
        result.setFitHeight(height);

        result.setX(posX);
        result.setY(posY);
        result.setPreserveRatio(false);

        return result;
    }

    private ImageView createErrorImageRectangle(double posX, double posY, double width, double height) {
        File localFail = WhereIsItConfigurable.getFolder(folder,"fail");

        if( localFail!=null ) {
            localFail = WhereIsItConfigurable.getFolder(localFail, "image");
        }

        if (localFail==null || !localFail.exists()) {
            localFail = new File(folder.getParentFile().getParentFile(), "/common/fail/images/");
        }
        final File[] files = WhereIsItConfigurable.getFiles(localFail);

        List<File> validImageFiles = new ArrayList<>();

        for (File file : files) {
            if (WhereIsItValidator.fileIsImageFile(file)) {
                validImageFiles.add(file);
            }
        }
        final File randomImageFile = validImageFiles.get(0);

        final Image image = new Image("file:" + randomImageFile.getAbsolutePath());

        ImageView result = new ImageView(image);

        result.setFitWidth(width);
        result.setFitHeight(height);

        double ratioX = result.getFitWidth() / image.getWidth();
        double ratioY = result.getFitHeight() / image.getHeight();

        double reducCoeff = Math.min(ratioX, ratioY);

        double w = image.getWidth() * reducCoeff;
        double h = image.getHeight() * reducCoeff;

        result.setX(posX);
        result.setY(posY);
        result.setTranslateX((result.getFitWidth() - w) / 2);
        result.setTranslateY((result.getFitHeight() - h) / 2);
        result.setPreserveRatio(true);

        result.setOpacity(0);
        result.setVisible(false);

        return result;
    }

    public void setIgnoreAnyInput(boolean b){
        customInputEventHandler.ignoreAnyInput = b;
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

    private class CustomInputEventHandler implements EventHandler<Event> {
        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        public boolean ignoreAnyInput = true;

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
