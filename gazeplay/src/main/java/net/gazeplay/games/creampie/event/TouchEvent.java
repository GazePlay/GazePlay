package net.gazeplay.games.creampie.event;

import gaze.GazeEvent;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Created by schwab on 17/08/2016.
 */
public class TouchEvent extends Event {

    public double x;
    public double y;

    /**
     * The only valid EventType for the CustomEvent.
     */
    public static final EventType<TouchEvent> TOUCH = new EventType<>(Event.ANY, "TOUCH");

    /**
     * Creates a new {@code LightningEvent} with an event type of {@code PLASMA_STRIKE}. The source and Target of the
     * event is set to {@code NULL_SOURCE_TARGET}.
     */
    public TouchEvent(double x, double y) {
        super(TOUCH);
        this.x = x;
        this.y = y;
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
    public TouchEvent(Object source, EventTarget target) {
        super(source, target, TOUCH);
    }

    @Override
    public TouchEvent copyFor(Object newSource, EventTarget newTarget) {
        return (TouchEvent) super.copyFor(newSource, newTarget);
    }

    @Override
    public EventType<? extends GazeEvent> getEventType() {
        return (EventType<? extends GazeEvent>) super.getEventType();
    }
}
