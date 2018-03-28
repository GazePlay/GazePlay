/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.colors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

/**
 *
 * @author medard
 */
public class GazeProgressIndicator extends ProgressIndicator implements IGazeProgressIndicator {

    private final double duration;

    private Timeline animation;

    private EventHandler<ActionEvent> finishHandler;

    public GazeProgressIndicator(double width, double height, final double duration) {
        super(0);

        this.setMinWidth(width);
        this.setMinHeight(width);
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

}
