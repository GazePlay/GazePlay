package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;

import java.util.List;

public class ProgressPortrait extends StackPane {

    @Getter
    private final Circle button;
    private ProgressIndicator indicator;
    private Timeline timelineProgressBar;
    private double buttonHeight;
    private EventHandler<Event> enterbuttonHandler;
    private EventHandler<Event> exitbuttonHandler;
    private boolean inuse = false;

    @Getter
    private ImageView image;


    public ProgressPortrait(int radius) {
        super();
        button = new Circle();
        button.setFill(Color.LIGHTGREY);
        init();
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

    public void disableProgressIndicator() {
        inuse = false;
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
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.setDisable(false);
        this.button.setDisable(false);
        this.setOpacity(1);
        this.indicator.setOpacity(0);

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
    }

    public void init() {
        final double buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        indicator.setMouseTransparent(true);
        timelineProgressBar = new Timeline();
        button.radiusProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(2 * newVal.doubleValue());
            indicator.setMinWidth(2 * newVal.doubleValue());
            buttonHeight = newVal.doubleValue();
            double width = newVal.doubleValue() * 2;
            width = (width * 90) / 100;
            image.setFitWidth(width);
        });

        indicator.setOpacity(0);
    }

    public ProgressIndicator assignIndicator(final EventHandler<Event> enterEvent, final int fixationLength) {
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        final ProgressPortrait pb = this;
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
        final ProgressPortrait pb = this;
        final Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterbuttonHandler = e -> {
            sizeProgressIndicator(gameContext);
            styleProgressIndicator(gameContext);
            if (inuse) {
                indicator.setProgress(0);
                indicator.setOpacity(0.5);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

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

    public Position getCurrentCenterPositionWithTranslation() {
        return new Position(
            (int) getLayoutX() + (int) getTranslateX() + getButton().getRadius(),
            (int) getLayoutY() + (int) getTranslateY() + getButton().getRadius());
    }

    protected Image pickRandomImage(final List<Image> availableImages) {
        final int count = availableImages.size();
        final ReplayablePseudoRandom r = new ReplayablePseudoRandom();
        final int index = r.nextInt(count);
        return availableImages.get(index);
    }

    public static ImageLibrary createImageLibrary(ReplayablePseudoRandom randomGenerator) {
        return ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"), randomGenerator);
    }
}
