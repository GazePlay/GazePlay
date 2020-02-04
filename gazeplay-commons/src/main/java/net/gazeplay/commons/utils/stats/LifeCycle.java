package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class LifeCycle {

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

    public void stop(final Runnable onAfterStop) {
        if (stopped.compareAndSet(false, true)) {
            if (started.get()) {
                stopTime = System.currentTimeMillis();
                onAfterStop.run();
            } else {
                throw new LifeCycleControlException("cannot stop when never started");
            }
        } else {
            // throw new LifeCycleControlException("already stopped");
        }
    }

    public long computeTotalElapsedDuration() {
        return Objects.requireNonNullElseGet(stopTime, System::currentTimeMillis) - startTime;
    }

}
