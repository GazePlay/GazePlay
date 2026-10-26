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
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;

/**
 * A round picture selected by a fixation: like {@link ProgressButton}, with the image fitted in the circle.
 */
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

    /**
     * Makes the portrait visible and selectable.
     */
    public void active() {
        inuse = true;
        this.setOpacity(1);
        this.indicator.setOpacity(0);
        this.setDisable(false);
        this.button.setDisable(false);
    }

    /**
     * Hides and disables the portrait if {@code b}, else makes it visible and selectable.
     */
    public void disable(final boolean b) {
        if (b) {
            disable();
        } else {
            active();
        }
    }

    /**
     * Hides the portrait and makes it unselectable.
     */
    public void disable() {
        inuse = false;
        this.setOpacity(0);
        this.setDisable(true);
        this.button.setDisable(true);
    }

    /**
     * Keeps the portrait visible, but no longer selectable.
     */
    public void disableProgressIndicator() {
        inuse = false;
    }

    /**
     * Applies the color of the progress indicator chosen in the settings.
     */
    public void styleProgressIndicator(IGameContext gameContext){
        indicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
    }

    /**
     * Applies the size of the progress indicator chosen in the settings (percentage of the size of the portrait).
     */
    public void sizeProgressIndicator(IGameContext gameContext){
        indicator.setMinHeight(getButton().getRadius() * gameContext.getConfiguration().getProgressBarSize() / 50);
        indicator.setMinWidth(getButton().getRadius() * gameContext.getConfiguration().getProgressBarSize() / 50);
    }

    /**
     * Replaces the image of the portrait.
     */
    public void setImage(final ImageView img) {
        image = img;
        image.setMouseTransparent(true);
        this.getChildren().set(1, image);
    }

    /**
     * Makes the portrait visible and listens to the gaze and the mouse entering and leaving it.
     */
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

    /**
     * Creates the progress indicator; the indicator and the image follow the size of the circle.
     */
    public void init() {
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

    /**
     * Sets the action run after a fixation of {@code fixationLength} ms (with a delay of 300 ms before the indicator
     * starts).
     *
     * @return the progress indicator
     */
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

    /**
     * Sets the action run after a fixation of the length of the settings, read at each fixation.
     *
     * @return the progress indicator
     */
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

    /**
     * @return the position of the center of the portrait, translation included
     */
    public Position getCurrentCenterPositionWithTranslation() {
        return new Position(
            (int) getLayoutX() + (int) getTranslateX() + getButton().getRadius(),
            (int) getLayoutY() + (int) getTranslateY() + getButton().getRadius());
    }

    /**
     * @return the library of the "portraits" folder of the images of the user, with the default images as fallback
     */
    public static ImageLibrary createImageLibrary(ReplayablePseudoRandom randomGenerator) {
        return ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"), randomGenerator);
    }
}
