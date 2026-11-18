package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Start and stop times of a game, each settable once, and the time spent in the menu of the game screen between them
 * (format 6 of the results, 17/11/2026: "menuDuration" of "lifeCycle" in replayData.json, not counted in the duration of
 * the game; absent from the files of versions 1 to 5, where the menu time was counted).
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

    /**
     * Time spent in the menu between the start and the stop, in ms; null before the stop, and in a file of version 1
     * to 5.
     */
    @Getter
    private Long menuDuration;

    /**
     * {@link MenuTime#total()} at the start (not read from the files).
     */
    private transient long menuTimeAtStart;

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
            menuTimeAtStart = MenuTime.total();
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
                menuDuration = MenuTime.total() - menuTimeAtStart;
                onAfterStop.run();
            } else {
                throw new LifeCycleControlException("cannot stop when never started");
            }
        }
        // stopping a stopped life cycle does nothing
    }

    /**
     * @return the time from the start to the stop, or to now when not stopped, without the time spent in the menu, in
     *   ms (a file of version 1 to 5 has no menu time: its whole time)
     */
    public long computeTotalElapsedDuration() {
        if (stopTime != null) {
            return stopTime - startTime - (menuDuration == null ? 0 : menuDuration);
        }
        return System.currentTimeMillis() - startTime - (MenuTime.total() - menuTimeAtStart);
    }

}
