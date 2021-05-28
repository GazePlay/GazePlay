package net.gazeplay.commons.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.ProgressButton;

public class CustomButton extends Rectangle {

    public ProgressIndicator indicator;
    private Timeline timelineProgressBar;
    private double buttonHeight;
    private EventHandler<Event> enterbuttonHandler;
    private EventHandler<Event> exitbuttonHandler;
    private boolean inuse = false;

    public CustomButton(String imageResourceLocation, Dimension2D screenDimension) {
        this(imageResourceLocation, screenDimension.getWidth() / 20d);

        ImagePattern value = new ImagePattern(new Image(imageResourceLocation), 0, 0, 1, 1, true);
        this.setFill(value);

        init();
    }

    public CustomButton(String imageResourceLocation, double size) {
        super(0, 0, size, size);

        ImagePattern value = new ImagePattern(new Image(imageResourceLocation), 0, 0, 1, 1, true);
        this.setFill(value);

        init();
    }

    public void recomputeSizeAndPosition(Scene scene) {
        double size = scene.getWidth() / 10;

        double positionX = scene.getWidth() - (size * 1.5);
        double positionY = scene.getHeight() - (size * 1.5);

        setX(positionX);
        setY(positionY);
        setWidth(size);
        setHeight(size);
    }

    public void active2() {
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.setDisable(false);
        this.setOpacity(1);
        this.indicator.setOpacity(0);

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
    }

    public ProgressIndicator assignIndicator(final EventHandler<Event> enterEvent, final int fixationLength) {
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        final CustomButton pb = this;
        final Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterbuttonHandler = e -> {
            if (inuse) {
                indicator.setProgress(0);
                indicator.setOpacity(0.5);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

                timelineProgressBar.setDelay(new Duration(300));

                timelineProgressBar.getKeyFrames().add(
                    new KeyFrame(new Duration(fixationLength), new KeyValue(indicator.progressProperty(), 1)));

                timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                    indicator.setOpacity(0);
                    if (enterEvent != null) {
                        enterEvent.handle(e1);
                    }
                });
                timelineProgressBar.play();
            }
        };

        exitbuttonHandler = e -> {
            if (inuse) {

                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);
            }
        };

        active2();

        return indicator;
    }

    public ProgressIndicator assignIndicatorUpdatable(final EventHandler<Event> enterEvent, final IGameContext gameContext) {
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        final CustomButton pb = this;
        final Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterbuttonHandler = e -> {
            if (inuse) {
                indicator.setProgress(0);
                indicator.setOpacity(0.5);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

                timelineProgressBar.setDelay(new Duration(300));

                timelineProgressBar.getKeyFrames().add(
                    new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(indicator.progressProperty(), 1)));

                timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                    indicator.setOpacity(0);
                    if (enterEvent != null) {
                        enterEvent.handle(e1);
                    }
                });
                timelineProgressBar.play();
            }
        };

        exitbuttonHandler = e -> {
            if (inuse) {

                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);
            }
        };

        active2();

        return indicator;
    }

    public ProgressIndicator assignIndicatorUpdatable(final EventHandler<Event> enterEvent) {
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        final CustomButton pb = this;
        final Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterbuttonHandler = e -> {
            if (inuse) {
                indicator.setProgress(0);
                indicator.setOpacity(0.5);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

                timelineProgressBar.setDelay(new Duration(300));

                timelineProgressBar.getKeyFrames().add(
                    new KeyFrame(new Duration(1000), new KeyValue(indicator.progressProperty(), 1)));

                timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                    indicator.setOpacity(0);
                    if (enterEvent != null) {
                        enterEvent.handle(e1);
                    }
                });
                timelineProgressBar.play();
            }
        };

        exitbuttonHandler = e -> {
            if (inuse) {

                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);
            }
        };

        active2();

        return indicator;
    }

    public void init() {
        final double buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        indicator.setMouseTransparent(true);
        timelineProgressBar = new Timeline();
        //button.radiusProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(this.getHeight());
            indicator.setMinWidth(this.getWidth());
            buttonHeight = this.getHeight();
            /*double width = this.getHeight() * 2;
            width = (width * 90) / 100;
            image.setFitWidth(width);*/
        //});

        indicator.setOpacity(0);
    }

    public void active() {
        inuse = true;
        this.setOpacity(1);
        this.indicator.setOpacity(0);
        this.setDisable(false);
        //this.button.setDisable(false);
    }

}
