package net.gazeplay.games.frog;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class Nenuphar extends Group {

    IGameContext gameContext;
    Frog frog;
    ImageView nenupharImgView;
    Boolean haveFrog = false;
    boolean ignoreInput = true;
    int indexNenuphar;
    ArrayList<String> eventNenuphar = new ArrayList<>();
    ArrayList<String> fixationLengthNenuphar = new ArrayList<>();
    private Timeline progressIndicatorAnimationTimeLine;
    private ProgressIndicator progressIndicator;

    EventType<? extends Event> inputEventEntered;
    EventType<? extends Event> inputEventMoved;
    EventType<? extends Event> inputEventExited;

    public Nenuphar(double screenWidth, double screenHeight, Image nenupharImg, int index, int nbNenuphars, IGameContext gameContext, Frog frog){
        this.gameContext = gameContext;
        this.frog = frog;
        this.indexNenuphar = index;

        this.drawNenuphars(screenWidth, screenHeight, nenupharImg, index, nbNenuphars);

        this.progressIndicator = buildProgressIndicator();
        gameContext.getChildren().add(progressIndicator);

        this.generateEvent();
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
        double progressWidth = 30.0;
        double progressHeight = 30.0;

        double positionX = nenupharImgView.getX() - progressWidth/2;
        double positionY = nenupharImgView.getY() + progressHeight/2;

        ProgressIndicator pi = new ProgressIndicator(0);
        pi.setTranslateX(positionX);
        pi.setTranslateY(positionY);
        pi.setTranslateZ(-1);
        pi.setOpacity(0.5);
        pi.setVisible(false);
        return pi;
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

            eventNenuphar.add("Validate");
            fixationLengthNenuphar.add(String.valueOf(gameContext.getConfiguration().getFixationLength()));
            frog.updateStats(indexNenuphar);

            this.newProgressIndicator();
            frog.frogPosition = frog.correctFrogPosition;
            frog.moveFrogTo(frog.nenuphars[frog.correctFrogPosition]);
            frog.iaTurn();
        };
    }


    public void newProgressIndicator() {
        this.gameContext.getChildren().remove(progressIndicator);
        this.progressIndicator = buildProgressIndicator();
        this.gameContext.getChildren().add(progressIndicator);
    }

    public void generateEvent(){
        if (Objects.equals(gameContext.getConfiguration().getEyeTracker(), "tobii")){
            inputEventEntered = GazeEvent.GAZE_ENTERED;
            inputEventMoved = GazeEvent.GAZE_MOVED;
            inputEventExited = GazeEvent.GAZE_EXITED;
        }else {
            inputEventEntered = MouseEvent.MOUSE_ENTERED;
            inputEventMoved = MouseEvent.MOUSE_MOVED;
            inputEventExited = MouseEvent.MOUSE_EXITED;
        }
    }

    private class CustomInputEventHandler implements EventHandler<Event> {

        private boolean moved = false;

        @Override
        public void handle(Event e) {

            if (!ignoreInput & !haveFrog) {
                if (e.getEventType() == inputEventEntered) {
                    log.info("Entered");
                    onEntered();
                } else if (e.getEventType() == inputEventMoved){
                    onEnteredOnceWhileMoved();
                } else if (e.getEventType() == inputEventExited) {
                    log.info("Exited");
                    onExited();
                }
            }
        }

        private void onEntered() {
            this.moved = true;
            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(frog);
            progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            progressIndicator.setProgress(0);
            progressIndicator.setVisible(true);
            progressIndicatorAnimationTimeLine.playFromStart();

            eventNenuphar.add("Entered");
            fixationLengthNenuphar.add("");
            frog.updateStats(indexNenuphar);
        }

        private void onEnteredOnceWhileMoved(){
            if (!this.moved){
                this.moved = true;
                progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(frog);
                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);
                progressIndicatorAnimationTimeLine.playFromStart();
            }
        }

        private void onExited() {

            double fixationPercentage = progressIndicator.getProgress();

            progressIndicatorAnimationTimeLine.stop();
            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);
            this.moved = false;

            if (fixationPercentage != 0.0){
                eventNenuphar.add("Exited");
                fixationLengthNenuphar.add(String.valueOf(gameContext.getConfiguration().getFixationLength() * fixationPercentage));
                frog.updateStats(indexNenuphar);
            }
        }

    }
}
