package net.gazeplay.games.frog;

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
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class Nenuphar extends Group {

    IGameContext gameContext;
    Frog frog;
    ImageView nenupharImgView;
    Boolean haveFrog = false;
    boolean ignoreInput = true;
    int indexNenuphar;

    private Timeline progressIndicatorAnimationTimeLine;
    private ProgressIndicator progressIndicator;

    public Nenuphar(double screenWidth, double screenHeight, Image nenupharImg, int index, int nbNenuphars, IGameContext gameContext, Frog frog){
        this.gameContext = gameContext;
        this.frog = frog;
        this.indexNenuphar = index;

        this.drawNenuphars(screenWidth, screenHeight, nenupharImg, index, nbNenuphars);

        this.progressIndicator = buildProgressIndicator();
        gameContext.getChildren().add(progressIndicator);

        CustomInputEventHandler customInputEventHandler = new CustomInputEventHandler();
        nenupharImgView.addEventFilter(MouseEvent.ANY, customInputEventHandler);
        nenupharImgView.addEventFilter(GazeEvent.ANY, customInputEventHandler);
    }

    public void drawNenuphars(double screenWidth, double screenHeight, Image nenupharImg, int index, int nbNenuphars){

        double radiusX = screenWidth / 3;
        double radiusY = screenHeight / 3;
        double centerX = screenWidth / 2;
        double centerY = screenHeight / 2;

        double nenupharSize = (Math.min(radiusX, radiusY) / 3) * 2;
        double angle = 2 * Math.PI / nbNenuphars * index;
        double x = centerX + radiusX * Math.cos(angle) - nenupharSize / 2;
        double y = centerY + radiusY * Math.sin(angle) - nenupharSize / 2;

        nenupharImgView = new ImageView(nenupharImg);
        nenupharImgView.setFitWidth(nenupharSize);
        nenupharImgView.setFitHeight(nenupharSize);
        nenupharImgView.setX(x);
        nenupharImgView.setY(y);
        nenupharImgView.setOpacity(0.5);
        gameContext.getChildren().add(nenupharImgView);
        gameContext.getGazeDeviceManager().addEventFilter(nenupharImgView);
    }

    private ProgressIndicator buildProgressIndicator() {
        // progressIndicator 2cm de diamètre
        double minWidth = 75;
        double minHeight = 75;

        double positionX = nenupharImgView.getX();
        double positionY = nenupharImgView.getY();

        ProgressIndicator result = new ProgressIndicator(0);
        result.setTranslateX(positionX);
        result.setTranslateY(positionY);
        result.setMinWidth(minWidth);
        result.setMinHeight(minHeight);
        result.setOpacity(0.5);
        result.setVisible(false);
        return result;
    }

    private Timeline createProgressIndicatorTimeLine(Frog frog) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(frog);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(Frog frog) {
        return actionEvent -> {
            this.newProgressIndicator();
            frog.frogPosition = this.indexNenuphar;
            frog.moveFrogTo(this);
            frog.iaTurn();
        };
    }

    public void newProgressIndicator() {
        this.gameContext.getChildren().remove(progressIndicator);
        this.progressIndicator = buildProgressIndicator();
        this.gameContext.getChildren().add(progressIndicator);
    }

    private class CustomInputEventHandler implements EventHandler<Event> {

        private boolean moved = false;

        @Override
        public void handle(Event e) {
            if (!ignoreInput & !haveFrog) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    onEntered();
                } else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    onExited();
                }
            }
        }

        private void onEntered() {
            this.moved = true;
            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(frog);
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
                progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(frog);
                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);
                progressIndicatorAnimationTimeLine.playFromStart();
            }
        }

        private void onExited() {
            progressIndicatorAnimationTimeLine.stop();
            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);
            this.moved = false;
        }

    }
}
