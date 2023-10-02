package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ProgressButton extends StackPane {

    @Getter
    private final Circle button;
    private ProgressIndicator indicator;
    private Timeline timelineProgressBar;
    private EventHandler<Event> enterButtonHandler;
    private EventHandler<Event> exitButtonHandler;
    private boolean inuse = false;

    @Getter
    private ImageView image;
    @Getter
    private String name;

    public ProgressButton() {
        this(true);
    }

    public ProgressButton(boolean imageResized) {
        super();
        button = new Circle();
        button.setFill(Color.LIGHTGREY);
        init(imageResized);
        image = new ImageView();
        this.getChildren().addAll(button, image, indicator);
    }

    public ProgressButton(String name){
        super();
        this.name = name;
        button = new Circle();
        button.setFill(Color.LIGHTGREY);
        init(true);
        image = new ImageView();
        this.getChildren().addAll(button, image, indicator);
    }

    public void active() {
        inuse = true;
        this.setOpacity(1);
        this.indicator.setOpacity(0);
        this.setDisable(false);
        this.button.setDisable(false);
    }

    public void disable(final boolean b) {
        if (b) {
            disable();
        } else {
            active();
        }
    }

    public void disable() {
        inuse = false;
        this.setOpacity(0);
        this.setDisable(true);
        this.button.setDisable(true);
    }

    public void styleProgressIndicator(IGameContext gameContext){
        indicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
    }

    public void sizeProgressIndicator(IGameContext gameContext){
        indicator.setMinHeight(getButton().getRadius() * gameContext.getConfiguration().getProgressBarSize() / 50);
        indicator.setMinWidth(getButton().getRadius() * gameContext.getConfiguration().getProgressBarSize() / 50);
    }

    public void setImage(final ImageView img) {
        image = img;
        image.setMouseTransparent(true);
        this.getChildren().set(1, image);
    }

    public void active2() {
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterButtonHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitButtonHandler);
        this.setDisable(false);
        this.button.setDisable(false);
        this.setOpacity(1);
        this.indicator.setOpacity(0);

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterButtonHandler);
        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitButtonHandler);
    }

    public void init(boolean imageResized) {
        indicator = new ProgressIndicator(0);
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        timelineProgressBar = new Timeline();
        button.radiusProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(2 * newVal.doubleValue());
            indicator.setMinWidth(2 * newVal.doubleValue());
            if (imageResized) {
                image.setFitWidth(1.8 * newVal.doubleValue());
            }
        });
    }

    public ProgressIndicator assignIndicatorUpdatable(final EventHandler<Event> enterEvent, Configuration config){
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        final ProgressButton pb = this;
        final Event e1 = new Event(pb, pb, MouseEvent.ANY);

        enterButtonHandler = e -> {
            indicator.setStyle(" -fx-progress-color: " + config.getProgressBarColor());
            if (inuse) {
                indicator.setProgress(0);
                indicator.setOpacity(0.5);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

                timelineProgressBar.setDelay(new Duration(300));

                timelineProgressBar.getKeyFrames().add(new KeyFrame(
                    new Duration(2000),
                    new KeyValue(indicator.progressProperty(), 1)
                ));

                timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                    indicator.setOpacity(0);
                    if (enterEvent != null) {
                        enterEvent.handle(e1);
                    }
                });
                timelineProgressBar.play();
            }
        };

        exitButtonHandler = e -> {
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
        final ProgressButton pb = this;
        final Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterButtonHandler = e -> {
            sizeProgressIndicator(gameContext);
            styleProgressIndicator(gameContext);
            if (inuse) {
                indicator.setProgress(0);
                indicator.setOpacity(0.5);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

                timelineProgressBar.setDelay(new Duration(300));

                timelineProgressBar.getKeyFrames().add(new KeyFrame(
                    new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(indicator.progressProperty(), 1)
                ));

                timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                    indicator.setOpacity(0);
                    if (enterEvent != null) {
                        enterEvent.handle(e1);
                    }
                });
                timelineProgressBar.play();
            }
        };

        exitButtonHandler = e -> {
            if (inuse) {
                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);
            }
        };

        active2();

        return indicator;
    }
}
