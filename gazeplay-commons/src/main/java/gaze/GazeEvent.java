package gaze;

/**
 * Created by schwab on 14/08/2016.
 */

import com.theeyetribe.clientsdk.IGazeListener;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;

public class GazeEvent extends InputEvent {

    static IGazeListener gazeListener;

    public static final EventType<GazeEvent> ANY = new EventType<GazeEvent>(Event.ANY, "GAZE");

    public static final EventType<GazeEvent> GAZE_ENTERED = new EventType<>(GazeEvent.ANY, "GAZE_ENTERED");

    public static final EventType<GazeEvent> GAZE_EXITED = new EventType<>(GazeEvent.ANY, "GAZE_EXITED");

    public static final EventType<GazeEvent> GAZE_MOVED = new EventType<>(GazeEvent.ANY, "GAZE_MOVED");

    private long time;

    private double X;

    private double Y;

    /**
     * Creates a new {@code LightningEvent} with an event type of {@code PLASMA_STRIKE}. The source and Target of the
     * event is set to {@code NULL_SOURCE_TARGET}.
     */
    public GazeEvent() {
        super(GAZE_ENTERED);
    }

    public GazeEvent(EventType<GazeEvent> et) {
        super(et);
    }

    public long getTime() {
        return time;
    }

    public GazeEvent(EventType<GazeEvent> et, long time) {
        super(et);
        this.time = time;

    }

    public GazeEvent(EventType<GazeEvent> et, long time, double X, double Y) {
        super(et);
        this.time = time;
        this.X = X;
        this.Y = Y;
    }

    /**
     * Construct a new {@code LightningEvent} with the specified event source and Target. If the source or Target is set
     * to {@code null}, it is replaced by the {@code NULL_SOURCE_TARGET} value. All LightningEvents have their type set
     * to {@code PLASMA_STRIKE}.
     *
     * @param source
     *            the event source which sent the event
     * @param target
     *            the event Target to associate with the event
     */
    public GazeEvent(Object source, EventTarget target) {
        super(source, target, GAZE_ENTERED);
    }

    @Override
    public GazeEvent copyFor(Object newSource, EventTarget newTarget) {
        return (GazeEvent) super.copyFor(newSource, newTarget);
    }

    @Override
    public EventType<? extends GazeEvent> getEventType() {
        return (EventType<? extends GazeEvent>) super.getEventType();
    }

    public double getX() {

        return X;
    }

    public double getY() {

        return Y;
    }

}