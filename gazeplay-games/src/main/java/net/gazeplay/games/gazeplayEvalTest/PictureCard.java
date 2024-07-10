package net.gazeplay.games.gazeplayEvalTest;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.Objects;

@Slf4j
@ToString
@Getter
class PictureCard extends Group {

    private final double minTime;
    private final IGameContext gameContext;
    private final GazeplayEvalGameVariant gameVariant;

    private final ImageView imageRectangle;

    private double initialWidth;
    private double initialHeight;

    private final double initialPositionX;
    private final double initialPositionY;

    private final Stats stats;
    private final String imageName;
    private final CustomInputEventHandlerMouse customInputEventHandlerMouse;
    private final GazePlayEvalTest gameInstance;
    private ProgressIndicator progressIndicator;
    private Timeline progressIndicatorAnimationTimeLine;
    private boolean selected;
    private boolean alreadySee;
    private double valueProgressIndicator = 2000;
    private double valueTranslateX = 0;
    private double valueTranslateY = 0;
    private boolean firstPosition;

    PictureCard(double posX, double posY, double width, double height, @NonNull IGameContext gameContext, @NonNull GazeplayEvalGameVariant gameVariant,
                @NonNull String imageName, Double fixationLength, @NonNull Stats stats, GazePlayEvalTest gameInstance, Boolean firstPosition) {

        log.info("imagePath = {}", imageName);

        final Configuration config = gameContext.getConfiguration();

        this.minTime = config.getFixationLength();
        this.initialPositionX = posX;
        this.initialPositionY = posY;
        this.initialWidth = width;
        this.initialHeight = height;
        this.selected = false;
        this.alreadySee = false;
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.stats = stats;
        this.gameInstance = gameInstance;
        this.imageName = imageName;
        this.valueProgressIndicator = fixationLength;
        this.firstPosition = firstPosition;

        this.imageRectangle = createImageView(this.initialPositionX, this.initialPositionY, this.initialWidth, this.initialHeight, imageName);
        this.progressIndicator = buildProgressIndicator(this.initialWidth, this.initialHeight);


        this.getChildren().add(imageRectangle);
        this.getChildren().add(progressIndicator);

        customInputEventHandlerMouse = new CustomInputEventHandlerMouse();

        gameContext.getGazeDeviceManager().addEventFilter(imageRectangle);

        this.addEventFilter(MouseEvent.ANY, customInputEventHandlerMouse);
        this.addEventFilter(GazeEvent.ANY, customInputEventHandlerMouse);

    }

    private Timeline createProgressIndicatorTimeLine(GazePlayEvalTest gameInstance) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(this.valueProgressIndicator), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            gameInstance);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(GazePlayEvalTest gameInstance) {
        return actionEvent -> {

            selected = true;
            imageRectangle.removeEventFilter(MouseEvent.ANY, customInputEventHandlerMouse);
            imageRectangle.removeEventFilter(GazeEvent.ANY, customInputEventHandlerMouse);
            gameContext.getGazeDeviceManager().removeEventFilter(imageRectangle);
            customInputEventHandlerMouse.ignoreAnyInput = true;
            progressIndicator.setVisible(false);

            this.onCardSelected();
            if (gameInstance.checkAllPictureCardChecked()){
                this.waitBeforeNextRound();
            }
        };
    }

    public void removeEventHandler(){
        customInputEventHandlerMouse.ignoreAnyInput = true;
    }

    public void onCardSelected() {
        gameInstance.calculScores(this.imageName);
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, gameInstance);
    }

    public void waitBeforeNextRound(){
        Configuration config = ActiveConfigurationContext.getInstance();

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            if(gameInstance.increaseIndexFileImage()){
                this.endGame();
            }else {
                gameInstance.stopDisplayDuration();
                gameInstance.stopGetGazePosition();
                gameInstance.getScreenHeatmapGaze();
                gameInstance.dispose();
                gameContext.clear();
                gameInstance.launch();
            }
        });
        gameInstance.removeEventHandlerPictureCard();
        transition.playFromStart();
    }

    private ImageView createImageView(double posX, double posY, double width, double height,
                                      @NonNull String imageName) {

        Configuration config = ActiveConfigurationContext.getInstance();
        File file = new File(config.getFileDir() + "\\evals\\" +  this.gameVariant.getNameGame() + "\\images\\" + imageName);
        final Image image = new Image(file.toURI().toString());

        ImageView result = new ImageView(image);

        result.setFitWidth(width/2);
        result.setFitHeight(height/2);
        result.setX(posX);
        result.setY(posY);
        result.setTranslateY(result.getFitHeight() / 2);
        result.setPreserveRatio(true);

        if (this.firstPosition){
            result.setTranslateX(result.getFitWidth());
        }else {
            result.setTranslateX(result.getFitWidth() / 2);
        }


        return result;
    }

    private ProgressIndicator buildProgressIndicator(double parentWidth, double parentHeight) {
        // progressIndicator 2cm de diamètre
        double minWidth = 75;
        double minHeight = 75;

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

    public void endGame() {

        progressIndicator.setVisible(false);
        gameInstance.stopDisplayDuration();
        gameInstance.stopGetGazePosition();
        gameInstance.getScreenHeatmapGaze();
        gameInstance.finalStats();
        gameContext.updateScore(stats, gameInstance);
        gameInstance.resetFromReplay();
        gameInstance.dispose();
        gameContext.clear();
        gameInstance.generateEndScreen();
    }

    private class CustomInputEventHandlerMouse implements EventHandler<Event> {

        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        private boolean ignoreAnyInput = false;
        private boolean moved = false;

        @Override
        public void handle(Event e) {
            if (ignoreAnyInput) {
                return;
            }

            if (selected) {
                return;
            }

            if (gameInstance.eyeTracker.equals("tobii")){
                if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    onEntered();
                } else if (e.getEventType() == GazeEvent.GAZE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (e.getEventType() == GazeEvent.GAZE_EXITED) {
                    onExited();
                }
            }else {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    onEntered();
                } else if (e.getEventType() == MouseEvent.MOUSE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
                    onExited();
                }
            }
        }

        private void onEntered() {
            this.moved = true;
            log.info("ENTERED {}", imageName);

            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);
            progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
            progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
            progressIndicator.setProgress(0);
            progressIndicator.setVisible(true);
            progressIndicatorAnimationTimeLine.playFromStart();
        }

        private void onEnteredOnceWhileMoved(){
            if (!this.moved){

                this.moved = true;
                log.info("MOVED {}", imageName);

                progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);
                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);
                progressIndicatorAnimationTimeLine.playFromStart();
            }
        }

        private void onExited() {
            log.info("EXITED {}", imageName);

            progressIndicatorAnimationTimeLine.stop();

            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);

            this.moved = false;
        }

    }
}
