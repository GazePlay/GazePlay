package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

/**
 * A round button selected by a fixation: a ring fills during the fixation length of the settings while the gaze (or
 * the mouse) stays on it, then the button's action runs. An image can be put on the button.
 * <p>
 * Since 3/11/2026 (maintainer's request), the ring is the one of the reviewed games ({@link FixationRing}: the main
 * color on a light disc); it replaces the progress indicator of before (a disc of the color of the settings, filling
 * up). Its size is still the size of the progress indicator of the settings.
 */
@Slf4j
public class ProgressButton extends StackPane {

    @Getter
    private final Circle button;
    private final Group ring = new Group();
    private final Circle ringBackground = new Circle();
    private final Arc ringArc = new Arc();
    private Timeline timelineProgressBar;
    private EventHandler<Event> enterButtonHandler;
    private EventHandler<Event> exitButtonHandler;
    private boolean inuse = false;
    /**
     * The size of the ring, as a percentage of the size of the button (the size of the progress indicator of the
     * settings, read at each fixation).
     */
    private double ringPercent = 100;

    @Getter
    private ImageView image;

    public ProgressButton() {
        this(true);
    }

    public ProgressButton(boolean imageResized) {
        super();
        button = new Circle();
        button.setFill(Color.LIGHTGREY);
        init(imageResized);
        image = new ImageView();
        this.getChildren().addAll(button, image, ring);
    }

    /**
     * Makes the button visible and selectable.
     */
    public void active() {
        inuse = true;
        this.setOpacity(1);
        hideRing();
        this.setDisable(false);
        this.button.setDisable(false);
    }

    /**
     * Hides and disables the button if {@code b}, else makes it visible and selectable.
     */
    public void disable(final boolean b) {
        if (b) {
            disable();
        } else {
            active();
        }
    }

    /**
     * Hides the button and makes it unselectable.
     */
    public void disable() {
        inuse = false;
        this.setOpacity(0);
        this.setDisable(true);
        this.button.setDisable(true);
    }

    /**
     * Kept for the games calling it: the ring takes the main color of the menus (the color of the progress indicator
     * of the settings is no longer used here).
     */
    public void styleProgressIndicator(IGameContext gameContext) {
        // The ring is styled by the stylesheet (-gp-accent)
    }

    /**
     * Applies the size of the progress indicator chosen in the settings (percentage of the size of the button) to the
     * ring.
     */
    public void sizeProgressIndicator(IGameContext gameContext) {
        ringPercent = gameContext.getConfiguration().getProgressBarSize();
        sizeRing(ringBase());
    }

    /**
     * @return the radius the ring is sized on: the one of the disc of the button, or, for a button sized by its width
     * and height without a disc (the stables of Horses), half of its smaller side (the progress indicator of before
     * filled the button; the ring used to be a dot there)
     */
    private double ringBase() {
        if (button.getRadius() > 0) {
            return button.getRadius();
        }
        final double width = getWidth() > 0 ? getWidth() : getPrefWidth();
        final double height = getHeight() > 0 ? getHeight() : getPrefHeight();
        return Math.max(0, Math.min(width, height) / 2);
    }

    private void sizeRing(final double buttonRadius) {
        final double radius = Math.max(4, buttonRadius * ringPercent / 100);
        final double width = Math.max(4, radius * 0.16);
        ringBackground.setRadius(radius);
        ringArc.setRadiusX(radius - width / 2);
        ringArc.setRadiusY(radius - width / 2);
        ringArc.setStrokeWidth(width);
    }

    private void hideRing() {
        ring.setVisible(false);
        ringArc.setLength(0);
    }

    /**
     * Replaces the image shown on the button.
     */
    public void setImage(final ImageView img) {
        image = img;
        image.setMouseTransparent(true);
        this.getChildren().set(1, image);
    }

    /**
     * Makes the button visible and listens to the gaze and the mouse entering and leaving it.
     */
    public void active2() {
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterButtonHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitButtonHandler);
        this.setDisable(false);
        this.button.setDisable(false);
        this.setOpacity(1);
        hideRing();

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterButtonHandler);
        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitButtonHandler);
    }

    /**
     * Creates the ring, sized with the button (and the image too when {@code imageResized}). The ring is in a group
     * holding its full disc, kept out of the layout of the button and centered on it, so that nothing moves while it
     * fills.
     */
    public void init(boolean imageResized) {
        ringBackground.setStyle("-fx-fill: rgba(255, 255, 255, 0.35); -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        ringArc.setType(ArcType.OPEN);
        ringArc.setFill(null);
        ringArc.setStrokeLineCap(StrokeLineCap.ROUND);
        ringArc.setStartAngle(90);
        ringArc.setStyle("-fx-stroke: -gp-accent;");
        ring.getChildren().setAll(ringBackground, ringArc);
        ring.setMouseTransparent(true);
        // Out of the layout of the button, centered on it: the bounds of an arc change a little while it fills (they
        // follow its curves), and the button used to center its content again at each change, so that its image
        // shook during the fixation (found by the maintainer on the die and the pawns of Horses, 3/11/2026)
        ring.setManaged(false);
        ring.layoutXProperty().bind(widthProperty().divide(2));
        ring.layoutYProperty().bind(heightProperty().divide(2));
        hideRing();
        timelineProgressBar = new Timeline();
        button.radiusProperty().addListener((obs, oldVal, newVal) -> {
            sizeRing(newVal.doubleValue());
            if (imageResized) {
                image.setFitWidth(1.8 * newVal.doubleValue());
            }
        });
    }

    /**
     * Sets the action of the button, run after a fixation of the length of the settings (read at each fixation, so a
     * change during the game applies), with a delay of 300 ms before the ring starts.
     *
     * @param enterEvent the action of the button, may be null
     */
    public void assignIndicatorUpdatable(final EventHandler<Event> enterEvent, final IGameContext gameContext) {
        hideRing();
        final ProgressButton pb = this;
        final Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterButtonHandler = e -> {
            sizeProgressIndicator(gameContext);
            if (inuse) {
                ringArc.setLength(0);
                ring.setVisible(true);

                timelineProgressBar.stop();
                timelineProgressBar.getKeyFrames().clear();

                timelineProgressBar.setDelay(new Duration(300));

                timelineProgressBar.getKeyFrames().add(new KeyFrame(
                    new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(ringArc.lengthProperty(), -360)
                ));

                timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                    hideRing();
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
                hideRing();
            }
        };

        active2();
    }

    /**
     * @return whether the ring of a fixation is shown (for the tests)
     */
    boolean isRingShown() {
        return ring.isVisible();
    }

    /**
     * @return whether the ring takes part in the layout of the button (for the tests: it must not)
     */
    boolean isRingManaged() {
        return ring.isManaged();
    }

    /**
     * @return the horizontal place of the center of the ring in the button (for the tests)
     */
    double getRingCenterX() {
        return ring.getLayoutX();
    }

    /**
     * @return the outer radius of the ring (for the tests)
     */
    double getRingRadius() {
        return ringBackground.getRadius();
    }

    /**
     * @return how much the ring is filled, from 0 to 1 (for the tests)
     */
    double getRingFill() {
        return -ringArc.getLength() / 360;
    }
}
