package net.gazeplay.games.colors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class GazeProgressIndicator extends ProgressIndicator implements IGazeProgressIndicator {

    private final double duration;

    private Timeline animation;

    private EventHandler<ActionEvent> finishHandler;

    public GazeProgressIndicator(double width, double height, final double duration) {
        super(0);

        this.setMinWidth(width);
        this.setMinHeight(height);
        this.setOpacity(0);

        this.duration = duration;
        this.animation = new Timeline();
    }

    @Override
    public void setOnFinish(EventHandler<ActionEvent> handler) {
        this.finishHandler = handler;
    }

    @Override
    public void start() {

        this.setOpacity(1);
        this.setProgress(0);

        animation.stop();
        animation = new Timeline();

        animation.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(this.progressProperty(), 1)));

        if (finishHandler != null) {
            animation.setOnFinished(finishHandler);
        }

        animation.play();
    }

    @Override
    public void stop() {

        animation.stop();
        this.setOpacity(0);
        this.setProgress(0);
    }
    
    public EventHandler buildEventHandler() {
        
        EventHandler event = (EventHandler) (Event event1) -> {
            
            if (event1.getEventType() == MouseEvent.MOUSE_ENTERED
                    || event1.getEventType() == GazeEvent.GAZE_ENTERED) {
                
                log.info("Entered");
                this.start();

            } else if (event1.getEventType() == MouseEvent.MOUSE_EXITED
                    || event1.getEventType() == GazeEvent.GAZE_EXITED) {

                log.info("Exited");
                this.stop();
            }
        };
                
        return event;
    }
}
