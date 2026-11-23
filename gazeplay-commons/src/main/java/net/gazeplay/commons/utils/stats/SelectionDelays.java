package net.gazeplay.commons.utils.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * The delays before a selection (23/11/2026, tester's feedback, point 22): while new choices appear, nothing can be
 * chosen for the delay of the settings, but the gaze and the mouse are still recorded. Their start and end are kept in
 * ms of the clock; {@link Stats} writes them in "selectionDelays" of replayData.json in ms since "gameStartedTime", as
 * the "time" of the samples of "coordinatesAndTimeStamp", so that an analysis can find or leave out these moments.
 * <p>
 * Static on purpose, as {@link MenuTime}: one game per JVM, and the delays are run by the game screen, which does not
 * know the statistics of the game.
 */
public final class SelectionDelays {

    private static final Object LOCK = new Object();

    private static LongSupplier clock = System::currentTimeMillis;

    /**
     * The delays over, each its start and its end.
     */
    private static final List<long[]> PERIODS = new ArrayList<>();

    /**
     * When the current delay started, or -1 when none runs.
     */
    private static long startedAt = -1;

    private SelectionDelays() {
    }

    /**
     * A delay starts; a delay started again while it runs goes on as the same period.
     */
    public static void begin() {
        synchronized (LOCK) {
            if (startedAt < 0) {
                startedAt = clock.getAsLong();
            }
        }
    }

    /**
     * The delay is over.
     */
    public static void end() {
        synchronized (LOCK) {
            if (startedAt >= 0) {
                PERIODS.add(new long[]{startedAt, clock.getAsLong()});
                startedAt = -1;
            }
        }
    }

    /**
     * @return the delays between two times (those of a round), cut at these times; a delay still running ends at
     *   {@code to}
     */
    public static List<long[]> between(final long from, final long to) {
        synchronized (LOCK) {
            final List<long[]> result = new ArrayList<>();
            final List<long[]> all = new ArrayList<>(PERIODS);
            if (startedAt >= 0) {
                all.add(new long[]{startedAt, to});
            }
            for (final long[] period : all) {
                final long start = Math.max(from, period[0]);
                final long end = Math.min(to, period[1]);
                if (end > start) {
                    result.add(new long[]{start, end});
                }
            }
            return result;
        }
    }

    /**
     * Replaces the clock and forgets the delays (tests only).
     */
    static void resetForTests(final LongSupplier testClock) {
        synchronized (LOCK) {
            clock = testClock;
            PERIODS.clear();
            startedAt = -1;
        }
    }
}
