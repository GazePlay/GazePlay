package net.gazeplay.games.whereisit;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Objects;

import static net.gazeplay.games.whereisit.WhereIsItGameType.*;

@Slf4j
@ToString
@Getter
class PictureCard extends Group {

    private final double minTime;
    private final IGameContext gameContext;
    private final boolean winner;

    private final Rectangle imageRectangle;
    private final Rectangle errorImageRectangle;

    private final double initialWidth;
    private final double initialHeight;

    private final double initialPositionX;
    private final double initialPositionY;

    private final Stats stats;
    private final String imagePath;

    private final ProgressIndicator progressIndicator;
    private Timeline progressIndicatorAnimationTimeLine;

    private boolean selected;

    private final PictureCard.CustomInputEventHandler customInputEventHandler;

    private final WhereIsIt gameInstance;

    PictureCard(double posX, double posY, double width, double height, @NonNull IGameContext gameContext,
                boolean winner, @NonNull String imagePath, @NonNull Stats stats, WhereIsIt gameInstance) {

        log.info("imagePath = {}", imagePath);

        final Configuration config = gameContext.getConfiguration();

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

        this.errorImageRectangle = createErrorImageRectangle();

        this.getChildren().add(imageRectangle);
        this.getChildren().add(progressIndicator);
        this.getChildren().add(errorImageRectangle);

        customInputEventHandler = new PictureCard.CustomInputEventHandler();

        gameContext.getGazeDeviceManager().addEventFilter(imageRectangle);

        this.addEventFilter(MouseEvent.ANY, customInputEventHandler);

        this.addEventFilter(GazeEvent.ANY, customInputEventHandler);
    }

    private Timeline createProgressIndicatorTimeLine(WhereIsIt gameInstance) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            gameInstance);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(WhereIsIt gameInstance) {
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

    private void onCorrectCardSelected(WhereIsIt gameInstance) {
        log.debug("WINNER");

        if (!gameInstance.getFirstWrong())
            gameInstance.updateRight();

        gameInstance.firstRightCardSelected();

        stats.incrementNumberOfGoalsReached();

        customInputEventHandler.ignoreAnyInput = true;
        progressIndicator.setVisible(false);

        gameInstance.removeAllIncorrectPictureCards();

        this.toFront();

        Dimension2D gamePanelDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("gamePanelDimension2D = {}", gamePanelDimension2D);

        ScaleTransition scaleToFullScreenTransition = new ScaleTransition(new Duration(1000), imageRectangle);
        double ratio = Math.max((gamePanelDimension2D.getWidth() / initialWidth), (gamePanelDimension2D.getHeight() / initialHeight));
        scaleToFullScreenTransition.setByX(ratio - 1);
        scaleToFullScreenTransition.setByY(ratio - 1);

        TranslateTransition translateToCenterTransition = new TranslateTransition(new Duration(1000),
            imageRectangle);
        translateToCenterTransition
            .setByX(-initialPositionX + (gamePanelDimension2D.getWidth() - initialWidth) / 2);
        translateToCenterTransition
            .setByY(-initialPositionY + (gamePanelDimension2D.getHeight() - initialHeight) / 2);

        ParallelTransition fullAnimation = new ParallelTransition();
        fullAnimation.getChildren().add(translateToCenterTransition);
        fullAnimation.getChildren().add(scaleToFullScreenTransition);

        if (gameInstance.iteration == 10){
            gameContext.updateScore(stats, gameInstance);
            gameInstance.dispose();
            gameContext.clear();
            gameInstance.endGame();
        }else {
            gameContext.updateScore(stats, gameInstance);
            gameInstance.dispose();
            gameContext.clear();
            gameInstance.launch();
        }

        fullAnimation.play();
    }

    private void onWrongCardSelected(WhereIsIt gameInstance) {
        //could be a single function?
        gameInstance.updateWrong();
        gameInstance.firstWrongCardSelected();

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

        fullAnimation.setOnFinished(actionEvent -> {
            if (gameContext.getConfiguration().isQuestionReaskedOnFail()) {
                gameInstance.playQuestionSound();
            }
            customInputEventHandler.ignoreAnyInput = false;
        });

        fullAnimation.play();
    }

    private Rectangle createImageView(double posX, double posY, double width, double height,
                                      @NonNull String imagePath) {

        Rectangle imgRec = new Rectangle(posX, posY, width, height);
        imgRec.setFill(new ImagePattern(new Image(imagePath)));

        return imgRec;
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

    private class CustomInputEventHandler implements EventHandler<Event> {

        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        private boolean ignoreAnyInput = false;

        EventType<? extends Event> inputEventEntered;
        EventType<? extends Event> inputEventExited;

        @Override
        public void handle(Event e) {

            if (Objects.equals(gameContext.getConfiguration().getEyeTracker(), "tobii")){
                inputEventEntered = GazeEvent.GAZE_ENTERED;
                inputEventExited = GazeEvent.GAZE_EXITED;
            }else {
                inputEventEntered = MouseEvent.MOUSE_EXITED;
                inputEventExited = MouseEvent.MOUSE_EXITED;
            }

            if (ignoreAnyInput) {
                return;
            }

            if (selected) {
                return;
            }

            if (e.getEventType() == inputEventEntered) {
                onEntered();
            } else if (e.getEventType() == inputEventExited) {
                onExited();
            }

        }

        private void onEntered() {
            log.info("ENTERED {}", imagePath);

            progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);

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
