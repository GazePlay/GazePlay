package net.gazeplay.games.gazeplayEval;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.function.BiConsumer;

/**
 * Something the participant selects by looking at it (a cell of a grid, the fixation cross, the start button, a
 * button of the choice screen), with the mouse too when the input mode accepts it.
 * <p>
 * The gaze and the mouse are followed separately: each entry and exit is recorded with its source. While at least one
 * of them is on the target and the target is active, an indicator fills in during the fixation time; the target is
 * selected when it is full. A target is inactive during the delay before selection, while a sound blocks the grid
 * and while the configuration menu is open.
 */
final class EvalTarget {

    /**
     * Receives the entries and exits ("entree", "sortie") and the selection of the target.
     */
    interface Listener {
        void onCrossing(EvalTarget target, String event, String source, double x, double y, long sampleTime);

        void onSelected(EvalTarget target, String source);
    }

    @Getter
    private final Shape area;

    @Getter
    private final String name;

    private final Listener listener;
    private final ProgressIndicator indicator;
    private final double fixationMillis;
    private final IGameContext gameContext;

    private boolean gazeInside;
    private boolean mouseInside;
    private String dwellSource;
    private Timeline dwell;
    @Getter
    private boolean active;
    @Getter
    private boolean closed;

    /**
     * @param area           the shape looked at, already in the game area (its bounds are those of the target)
     * @param fixationMillis fixation time that selects the target, in ms (0: as soon as it is looked at; negative: the
     *                       target is never selected, its entries and exits are only recorded)
     * @param indicatorLayer where the indicator is drawn, above the target
     */
    EvalTarget(IGameContext gameContext, Shape area, String name, double fixationMillis, Pane indicatorLayer,
               Listener listener) {
        this.gameContext = gameContext;
        this.area = area;
        this.name = name;
        this.fixationMillis = fixationMillis;
        this.listener = listener;

        indicator = new ProgressIndicator(0);
        indicator.setMouseTransparent(true);
        indicator.setVisible(false);
        indicator.setOpacity(0.7);
        double size = Math.max(40, 100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
        indicator.setMinSize(size, size);
        indicator.setPrefSize(size, size);
        indicator.setStyle("-fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        Bounds bounds = area.getBoundsInParent();
        indicator.setLayoutX(bounds.getMinX() + (bounds.getWidth() - size) / 2);
        indicator.setLayoutY(bounds.getMinY() + (bounds.getHeight() - size) / 2);
        indicatorLayer.getChildren().add(indicator);

        area.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> onEnter(EvalSessionRecorder.MOUSE, e.getX(), e.getY(), 0));
        area.addEventHandler(MouseEvent.MOUSE_EXITED, e -> onExit(EvalSessionRecorder.MOUSE, e.getX(), e.getY(), 0));
        area.addEventHandler(GazeEvent.GAZE_ENTERED, e -> onEnter(EvalSessionRecorder.GAZE, e.getX(), e.getY(), e.getSampleTime()));
        area.addEventHandler(GazeEvent.GAZE_EXITED, e -> onExit(EvalSessionRecorder.GAZE, e.getX(), e.getY(), e.getSampleTime()));
        gameContext.getGazeDeviceManager().addEventFilter(area);
    }

    private void onEnter(String source, double x, double y, long sampleTime) {
        if (closed) {
            return;
        }
        boolean wasInside = isLookedAt();
        setInside(source, true);
        listener.onCrossing(this, "entree", source, toGameX(x), toGameY(y), sampleTime);
        if (!wasInside) {
            dwellSource = source;
            startDwell();
        }
    }

    private void onExit(String source, double x, double y, long sampleTime) {
        if (closed || !isInside(source)) {
            return;
        }
        setInside(source, false);
        listener.onCrossing(this, "sortie", source, toGameX(x), toGameY(y), sampleTime);
        if (!isLookedAt()) {
            stopDwell();
        } else if (source.equals(dwellSource)) {
            dwellSource = gazeInside ? EvalSessionRecorder.GAZE : EvalSessionRecorder.MOUSE;
        }
    }

    // Positions of the events: in the coordinates of the area, which is directly in the game area
    private double toGameX(double x) {
        return area.localToParent(x, 0).getX();
    }

    private double toGameY(double y) {
        return area.localToParent(0, y).getY();
    }

    private boolean isInside(String source) {
        return EvalSessionRecorder.GAZE.equals(source) ? gazeInside : mouseInside;
    }

    private void setInside(String source, boolean inside) {
        if (EvalSessionRecorder.GAZE.equals(source)) {
            gazeInside = inside;
        } else {
            mouseInside = inside;
        }
    }

    /**
     * @return whether the gaze or the mouse is on the target
     */
    boolean isLookedAt() {
        return gazeInside || mouseInside;
    }

    /**
     * Allows or forbids the selection; when allowed while the target is looked at, the fixation starts.
     */
    void setActive(boolean active) {
        if (closed || this.active == active) {
            return;
        }
        this.active = active;
        if (active) {
            if (isLookedAt()) {
                if (dwellSource == null) {
                    dwellSource = gazeInside ? EvalSessionRecorder.GAZE : EvalSessionRecorder.MOUSE;
                }
                startDwell();
            }
        } else {
            stopDwell();
        }
    }

    private void startDwell() {
        if (!active || closed || fixationMillis < 0) {
            return;
        }
        stopDwell();
        indicator.setProgress(0);
        indicator.setVisible(fixationMillis > 0);
        dwell = new Timeline(new KeyFrame(Duration.millis(Math.max(1, fixationMillis)),
            new KeyValue(indicator.progressProperty(), 1)));
        dwell.setOnFinished(e -> {
            indicator.setVisible(false);
            if (active && !closed && isLookedAt()) {
                listener.onSelected(this, dwellSource == null ? EvalSessionRecorder.MOUSE : dwellSource);
            }
        });
        dwell.playFromStart();
    }

    private void stopDwell() {
        if (dwell != null) {
            dwell.stop();
            dwell = null;
        }
        indicator.setVisible(false);
        indicator.setProgress(0);
    }

    /**
     * Ends the target: it no longer reacts and no longer receives the gaze.
     */
    void close() {
        if (closed) {
            return;
        }
        stopDwell();
        active = false;
        closed = true;
        gameContext.getGazeDeviceManager().removeEventFilter(area);
    }
}
