package net.gazeplay.games.colors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;

public class AbstractGazeIndicator extends ProgressIndicator implements IGazeProgressIndicator {
    
    private double duration;

    private Timeline animation;

    private EventHandler<ActionEvent> finishHandler;
    
    public AbstractGazeIndicator() {
        super(0);

        this.setVisible(false);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        this.duration = config.getFixationlength();
        this.animation = new Timeline();
    }
    
    @Override
    public void setOnFinish(EventHandler<ActionEvent> handler) {
        this.finishHandler = handler;
    }

    @Override
    public void start() {

        this.setVisible(true);
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
        this.setVisible(false);
        this.setProgress(0);
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
