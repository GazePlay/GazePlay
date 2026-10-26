package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Start and stop times of a game, each settable once.
 */
public class LifeCycle {

    /**
     * Thrown when the life cycle is started twice, or stopped before being started.
     */
    public static class LifeCycleControlException extends RuntimeException {

        public LifeCycleControlException(final String message) {
            super(message);
        }

        public LifeCycleControlException(final String message, final Throwable cause) {
            super(message, cause);
        }

    }

    @Getter
    private Long startTime;

    @Getter
    private Long stopTime;

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * Runs {@code onBeforeStart}, then records the start time.
     *
     * @throws LifeCycleControlException when already started
     */
    public void start(final Runnable onBeforeStart) {
        if (started.compareAndSet(false, true)) {
            onBeforeStart.run();
            startTime = System.currentTimeMillis();
        } else {
            if (stopped.get()) {
                throw new LifeCycleControlException("cannot restart a " + this.getClass().getSimpleName());
            }
            throw new LifeCycleControlException("already started");
        }
    }

    /**
     * Records the stop time, then runs {@code onAfterStop}. Does nothing when already stopped.
     *
     * @throws LifeCycleControlException when never started
     */
    public void stop(final Runnable onAfterStop) {
        if (stopped.compareAndSet(false, true)) {
            if (started.get()) {
                stopTime = System.currentTimeMillis();
                onAfterStop.run();
            } else {
                throw new LifeCycleControlException("cannot stop when never started");
            }
        }
        // stopping a stopped life cycle does nothing
    }

    /**
     * @return the time from the start to the stop, or to now when not stopped, in ms
     */
    public long computeTotalElapsedDuration() {
        return Objects.requireNonNullElseGet(stopTime, System::currentTimeMillis) - startTime;
    }

}
