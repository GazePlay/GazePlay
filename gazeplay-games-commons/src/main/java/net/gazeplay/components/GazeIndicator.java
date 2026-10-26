package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Progress indicator of a fixation: it fills during the fixation length of the settings while the gaze (or the mouse)
 * stays on a node, then calls its finish handler and stays shown one second. It empties when the gaze leaves.
 */
@Slf4j
public class GazeIndicator extends ProgressIndicator implements IGazeProgressIndicator {

    /**
     * Not used by the indicator itself: free for the games that need to keep a duration with it.
     */
    @Getter
    @Setter
    private double duration;

    private Timeline animation;

    private EventHandler<ActionEvent> finishHandler;
    private final OnFinishIndicatorEvent onFinishHandler;

    private final Map<Node, EventHandler<Event>> nodedToListenTo;

    public static final long TIME_DISPLAYED_AFTER_FINISHED_MS = 1000;

    @Getter
    private boolean isStarted;
    private final IGameContext gameContext;

    public GazeIndicator(final IGameContext gameContext) {
        super(0);

        this.setVisible(false);

        this.gameContext = gameContext;
        this.animation = new Timeline();
        this.nodedToListenTo = new HashMap<>();

        onFinishHandler = buildOnFinishEventHandler();
    }

    @Override
    public void setOnFinish(final EventHandler<ActionEvent> handler) {
        this.finishHandler = handler;
    }

    @Override
    public void start() {

        this.setVisible(true);
        this.setProgress(0);

        animation.stop();
        animation = new Timeline();

        animation.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(this.progressProperty(), 1)));

        animation.setOnFinished(onFinishHandler);
        animation.play();
        isStarted = true;
    }

    @Override
    public void stop() {

        onFinishHandler.cancelHide();

        animation.stop();
        this.setVisible(false);
        this.setProgress(0);
        isStarted = false;
    }

    private OnFinishIndicatorEvent buildOnFinishEventHandler() {

        return new OnFinishIndicatorEvent(this);
    }

    private class OnFinishIndicatorEvent implements EventHandler<ActionEvent> {

        // Hides the indicator on the JavaFX thread: a java.util.Timer used to do it from its own thread
        private final PauseTransition hideDelay;

        public OnFinishIndicatorEvent(final GazeIndicator thisIndicator) {

            hideDelay = new PauseTransition(Duration.millis(TIME_DISPLAYED_AFTER_FINISHED_MS));
            hideDelay.setOnFinished(actionEvent -> thisIndicator.stop());
        }

        @Override
        public void handle(final ActionEvent event) {
            isStarted = false;
            if (finishHandler != null) {
                finishHandler.handle(event);
            }

            hideDelay.playFromStart();
        }

        private void cancelHide() {
            hideDelay.stop();
        }

    }

    /**
     * @return a handler starting the indicator when the gaze or the mouse enters the node, and stopping it when it
     *   leaves
     */
    protected EventHandler<Event> buildEventHandler(final Node node) {

        final GazeIndicator thisIndicator = this;

        return (Event event) -> {

            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                thisIndicator.start();
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                thisIndicator.stop();
            }
        };
    }

    @Override
    public boolean addNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager) {

        final EventHandler<Event> eventHandler = buildEventHandler(node);

        node.addEventFilter(MouseEvent.ANY, eventHandler);
        node.addEventFilter(GazeEvent.ANY, eventHandler);

        this.nodedToListenTo.put(node, eventHandler);

        gazeDeviceManager.addEventFilter(node);

        return true;
    }

    @Override
    public boolean removeNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager) {

        final EventHandler<Event> eventHandler = this.nodedToListenTo.remove(node);
        node.removeEventFilter(MouseEvent.ANY, eventHandler);
        node.removeEventFilter(GazeEvent.ANY, eventHandler);

        gazeDeviceManager.removeEventFilter(node);

        return true;
    }
}
