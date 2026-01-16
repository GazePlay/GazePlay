package net.gazeplay.games.gazeplayEval;

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
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.Objects;

@Slf4j
public class ScreenCard extends Group {

    private ImageView imageRectangle;
    private Rectangle gazeArea;
    private ProgressIndicator progressIndicator;
    private String type;
    private GazeplayEval gameInstance;
    private final IGameContext gameContext;
    private Timeline progressIndicatorAnimationTimeLine;
    private CustomInputEventHandler customInputEventHandler;
    private int timeFixationCross;
    private final GazeplayEvalGameVariant gameVariant;

    ScreenCard(double posX, double posY, double width, double height, @NonNull IGameContext gameContext, @NonNull GazeplayEvalGameVariant gameVariant,
               @NonNull String imageName, GazeplayEval gameInstance, String type, Boolean firstPosition, Boolean setFixaCross, int timeFixaCross){

        this.type = type;
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.timeFixationCross = timeFixaCross;

        this.imageRectangle = createCenteredImageView(imageName);

        if (setFixaCross) {
            this.gazeArea = createGazeArea(imageName);
            this.progressIndicator = buildProgressIndicator(posX, posY);
            customInputEventHandler = new CustomInputEventHandler();
            gameContext.getGazeDeviceManager().addEventFilter(gazeArea);
            this.addEventFilter(MouseEvent.ANY, customInputEventHandler);
            this.addEventFilter(GazeEvent.ANY, customInputEventHandler);
            this.getChildren().add(gazeArea);
            this.getChildren().add(imageRectangle);
            this.getChildren().add(progressIndicator);
        }else {
            this.getChildren().add(imageRectangle);
        }

    }

    public ImageView createCenteredImageView(@NonNull String imageName) {

        final Image image;
        if (Objects.equals(this.type, "cross")) {
            image = new Image("data/common/images/" + imageName);
        }else {
            Configuration config = ActiveConfigurationContext.getInstance();
            File file = new File(config.getFileDir() + "\\evals\\" +  this.gameVariant.getNameGame() + "\\images\\" + imageName);
            image = new Image(file.toURI().toString());
        }

        ImageView result = new ImageView(image);

        final Region root = gameContext.getRoot();

        result.setX((root.getWidth()/2) - (image.getWidth()/2));
        result.setY((root.getHeight()/2) - (image.getHeight()/2));
        result.setPreserveRatio(true);

        return result;
    }

    public Rectangle createGazeArea(@NonNull String imageName){
        final Image image;
        if (Objects.equals(this.type, "cross")) {
            image = new Image("data/common/images/" + imageName);
        }else {
            Configuration config = ActiveConfigurationContext.getInstance();
            File file = new File(config.getFileDir() + "\\evals\\" +  this.gameVariant.getNameGame() + "\\images\\" + imageName);
            image = new Image(file.toURI().toString());
        }

        gazeArea = new Rectangle();
        gazeArea.setX(this.imageRectangle.getX() - 25);
        gazeArea.setY(this.imageRectangle.getY() - 25);
        gazeArea.setWidth(image.getWidth() + 50);
        gazeArea.setHeight(image.getHeight() + 50);

        gazeArea.setFill(Color.TRANSPARENT);
        gazeArea.setStroke(Color.BLUE);
        gazeArea.setOpacity(0.0);

        return gazeArea;
    }

    private ProgressIndicator buildProgressIndicator(double parentWidth, double parentHeight) {
        // progressIndicator 2cm de diamètre
        double minWidth = 75;
        double minHeight = 75;

        final Region root = gameContext.getRoot();

        ProgressIndicator result = new ProgressIndicator(0);
        result.setTranslateX((root.getWidth()/2) - (minWidth/2));
        result.setTranslateY((root.getHeight()/2) - (minHeight/2));
        result.setMinWidth(minWidth);
        result.setMinHeight(minHeight);
        result.setOpacity(0.5);
        result.setVisible(false);

        return result;
    }

    private Timeline createProgressIndicatorTimeLine(GazeplayEval gameInstance) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(this.timeFixationCross * 1000), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            gameInstance);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(GazeplayEval gameInstance) {
        return actionEvent -> {

            imageRectangle.removeEventFilter(MouseEvent.ANY, customInputEventHandler);
            imageRectangle.removeEventFilter(GazeEvent.ANY, customInputEventHandler);
            gameContext.getGazeDeviceManager().removeEventFilter(imageRectangle);
            customInputEventHandler.ignoreAnyInput = true;
            progressIndicator.setVisible(false);

            if (Objects.equals(this.type, "transition")){
                gameInstance.stopTransitionTimeline();
            } else if (Objects.equals(this.type, "instruction")) {
                gameInstance.stopInstructionTimeline();
            }

            gameInstance.getScreenHeatmapGaze();
            gameInstance.clearScreen();
            gameInstance.increaseIndex();
            gameInstance.generateScreen();
        };
    }

    private class CustomInputEventHandler implements EventHandler<Event> {

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
            log.info("Entered in Cross !!!");
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
                log.info("Moved in Cross !!!");
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
            log.info("Exited of Cross !!!");
            progressIndicatorAnimationTimeLine.stop();

            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);

            this.moved = false;
        }

    }
}
