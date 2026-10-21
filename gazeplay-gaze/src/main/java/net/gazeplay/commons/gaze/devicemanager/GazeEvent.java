package net.gazeplay.commons.gaze.devicemanager;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import lombok.Getter;
import lombok.ToString;

@ToString
public class GazeEvent extends InputEvent {

    public static final EventType<GazeEvent> ANY = new EventType<>(InputEvent.ANY, "GAZE");

    public static final EventType<GazeEvent> GAZE_MOVED = new EventType<>(GazeEvent.ANY, "GAZE_MOVED");

    public static final EventType<GazeEvent> GAZE_ENTERED_TARGET = new EventType<>(GazeEvent.ANY,
        "GAZE_ENTERED_TARGET");

    public static final EventType<GazeEvent> GAZE_EXITED_TARGET = new EventType<>(GazeEvent.ANY,
        "GAZE_EXITED_TARGET");

    public static final EventType<GazeEvent> GAZE_ENTERED = new EventType<>(GazeEvent.GAZE_ENTERED_TARGET,
        "GAZE_ENTERED");

    public static final EventType<GazeEvent> GAZE_EXITED = new EventType<>(GazeEvent.GAZE_EXITED_TARGET, "GAZE_EXITED");

    @Getter
    private final long time;

    @Getter
    private final double x;

    @Getter
    private final double y;

    /**
     * Creates a new {@code LightningEvent} with an event type of {@code PLASMA_STRIKE}. The source and Target of the
     * event is set to {@code NULL_SOURCE_TARGET}.
     */
    public GazeEvent() {
        this(GAZE_ENTERED);
    }

    public GazeEvent(final EventType<GazeEvent> et) {
        this(et, 0);
    }

    public GazeEvent(final EventType<GazeEvent> et, final long time) {
        this(et, time, 0, 0);
    }

    public GazeEvent(final EventType<GazeEvent> et, final long time, final double x, final double y) {
        super(et);
        this.time = time;
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a new {@code LightningEvent} with the specified event source and Target. If the source or Target is set
     * to {@code null}, it is replaced by the {@code NULL_SOURCE_TARGET} value. All LightningEvents have their type set
     * to {@code PLASMA_STRIKE}.
     *
     * @param source the event source which sent the event
     * @param target the event Target to associate with the event
     */
    public GazeEvent(final Object source, final EventTarget target) {
        super(source, target, GAZE_ENTERED);
        this.time = 0;
        this.x = 0;
        this.y = 0;
    }

    @Override
    public GazeEvent copyFor(final Object newSource, final EventTarget newTarget) {
        return (GazeEvent) super.copyFor(newSource, newTarget);
    }

    @Override
    public EventType<? extends GazeEvent> getEventType() {
        return (EventType<? extends GazeEvent>) super.getEventType();
    }

}
