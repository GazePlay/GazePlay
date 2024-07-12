package net.gazeplay.games.training;

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
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

@Slf4j
public class TrainingEmmanuelGenerateLvl1 extends Group {

    private int[][] newMap;
    private final CustomInputEventHandler customInputEventHandler;
    private ProgressIndicator progressIndicator;
    public EventItemEmmanuel ruby;
    public IGameContext gameContext;
    public String eyeTracker;
    public TrainingEmmanuel gameInstance;
    private Timeline progressIndicatorAnimationTimeLine;
    public ImageView imageRectangle;

    public TrainingEmmanuelGenerateLvl1(){
        this.customInputEventHandler = new CustomInputEventHandler();
        this.eyeTracker = ActiveConfigurationContext.getInstance().getEyeTracker();
    }

    public void initiateLevel(){
        this.newMap = new int[][]{
            // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
    }

    public int[][] generateLabyrinth(
        IGameContext gameContext,
        ArrayList<EventItemEmmanuel> listEI,
        ArrayList<Rectangle> listWall,
        double sizeWw,
        double sizeWh,
        EventHandler<ActionEvent> eventwin,
        Stats stats,
        TrainingEmmanuel trainingEmmanuel){
        this.gameInstance = trainingEmmanuel;

        this.gameContext = gameContext;
        this.initiateLevel();

        this.imageRectangle = createImageView("data/follow/keyred.png");
        this.progressIndicator = buildProgressIndicator();

        this.getChildren().add(imageRectangle);
        this.getChildren().add(progressIndicator);

        gameContext.getGazeDeviceManager().addEventFilter(imageRectangle);

        this.addEventFilter(MouseEvent.ANY, customInputEventHandler);
        this.addEventFilter(GazeEvent.ANY, customInputEventHandler);

        return newMap;
    }

    private ImageView createImageView(@NonNull String imagePath) {
        final Image image = new Image(imagePath);

        ImageView result = new ImageView(image);
        result.setFitWidth(image.getWidth()*2);
        result.setFitHeight(image.getHeight()*2);

        final Region root = gameContext.getRoot();

        result.setX((root.getWidth()/2) - (result.getFitWidth()/2));
        result.setY((root.getHeight()/2) - (result.getFitHeight()/2));
        result.setPreserveRatio(true);

        return result;
    }

    private ProgressIndicator buildProgressIndicator() {

        double positionX = imageRectangle.getX();
        double positionY = imageRectangle.getY();

        ProgressIndicator result = new ProgressIndicator(0);
        result.setTranslateX(positionX + 50);
        result.setTranslateY(positionY + 50);
        result.setOpacity(0.5);
        result.setVisible(false);
        return result;
    }

    private Timeline createProgressIndicatorTimeLine(TrainingEmmanuel gameInstance) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            gameInstance);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(TrainingEmmanuel gameInstance) {
        return actionEvent -> {
            imageRectangle.removeEventFilter(MouseEvent.ANY, customInputEventHandler);
            imageRectangle.removeEventFilter(GazeEvent.ANY, customInputEventHandler);
            gameContext.getGazeDeviceManager().removeEventFilter(imageRectangle);

            gameInstance.win();
        };
    }

    private class CustomInputEventHandler implements EventHandler<Event> {

        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        public boolean ignoreAnyInput = false;
        private boolean moved = false;

        @Override
        public void handle(Event e) {

            if (ignoreAnyInput) {
                return;
            }

            if (eyeTracker.equals("tobii")){
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
            log.info("Entered !!!");
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
                log.info("Mouved !!!");
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
            log.info("Exited !!!");
            progressIndicatorAnimationTimeLine.stop();

            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);

            this.moved = false;
        }

    }
}
