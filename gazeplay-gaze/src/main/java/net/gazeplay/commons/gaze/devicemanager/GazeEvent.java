package net.gazeplay.commons.gaze.devicemanager;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import lombok.Getter;
import lombok.ToString;

/**
 * JavaFX event of the gaze (or of the mouse used as a gaze) on a node, fired by the gaze device manager on the nodes
 * registered with it and on the root of the game. Games listen to it as to mouse events: GAZE_ENTERED when the
 * position enters a node, GAZE_MOVED while it stays on it, GAZE_EXITED when it leaves it.
 */
@ToString
public class GazeEvent extends InputEvent {

    /**
     * Parent type of all the gaze events.
     */
    public static final EventType<GazeEvent> ANY = new EventType<>(InputEvent.ANY, "GAZE");

    public static final EventType<GazeEvent> GAZE_MOVED = new EventType<>(GazeEvent.ANY, "GAZE_MOVED");

    public static final EventType<GazeEvent> GAZE_ENTERED_TARGET = new EventType<>(GazeEvent.ANY,
        "GAZE_ENTERED_TARGET");

    public static final EventType<GazeEvent> GAZE_EXITED_TARGET = new EventType<>(GazeEvent.ANY,
        "GAZE_EXITED_TARGET");

    public static final EventType<GazeEvent> GAZE_ENTERED = new EventType<>(GazeEvent.GAZE_ENTERED_TARGET,
        "GAZE_ENTERED");

    public static final EventType<GazeEvent> GAZE_EXITED = new EventType<>(GazeEvent.GAZE_EXITED_TARGET, "GAZE_EXITED");

    /**
     * Time the position entered the node (GAZE_ENTERED, GAZE_MOVED), -1 when it left it (GAZE_EXITED): used by the
     * games for the fixation length.
     */
    @Getter
    private final long time;

    /**
     * Position in the coordinates of the node the event is fired on (0 when unknown).
     */
    @Getter
    private final double x;

    @Getter
    private final double y;

    /**
     * Acquisition time of the sample ({@link GazeSample#time()}, System.currentTimeMillis()), 0 when unknown. It can
     * precede the handling of the event by a few milliseconds, or more when the JavaFX thread is busy.
     */
    @Getter
    private final long sampleTime;

    /**
     * Creates a GAZE_ENTERED event, at no time and position.
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
        this(et, time, x, y, 0);
    }

    public GazeEvent(final EventType<GazeEvent> et, final long time, final double x, final double y, final long sampleTime) {
        super(et);
        this.time = time;
        this.x = x;
        this.y = y;
        this.sampleTime = sampleTime;
    }

    /**
     * Creates a GAZE_ENTERED event, at no time and position, with a source and a target (a null source or target is
     * replaced by JavaFX with its NULL_SOURCE_TARGET).
     *
     * @param source the event source which sent the event
     * @param target the event target to associate with the event
     */
    public GazeEvent(final Object source, final EventTarget target) {
        super(source, target, GAZE_ENTERED);
        this.time = 0;
        this.x = 0;
        this.y = 0;
        this.sampleTime = 0;
    }

    @Override
    public GazeEvent copyFor(final Object newSource, final EventTarget newTarget) {
        return (GazeEvent) super.copyFor(newSource, newTarget);
    }

    // Safe: a GazeEvent is only created with the GazeEvent event types (constructors)
    @SuppressWarnings("unchecked")
    @Override
    public EventType<? extends GazeEvent> getEventType() {
        return (EventType<? extends GazeEvent>) super.getEventType();
    }

}
