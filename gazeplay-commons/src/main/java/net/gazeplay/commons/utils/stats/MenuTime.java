package net.gazeplay.commons.utils.stats;

import java.util.function.LongSupplier;

/**
 * The time spent with the menu of the game screen (Esc) open. Since the format 6 of the results (17/11/2026, decision
 * of the maintainer), it is not counted in the durations: the durations of the rounds are measured on a clock that
 * stops while the menu is open ({@link #gameClock()}), and the duration of the game is its stop minus its start minus
 * the time spent in the menu ({@link LifeCycle}).
 * <p>
 * Static on purpose, as the state of the menu in {@link Stats}: one game per JVM, and the menu belongs to the game
 * screen, built before the games create their statistics.
 */
public final class MenuTime {

    private static final Object LOCK = new Object();

    private static LongSupplier clock = System::currentTimeMillis;

    /**
     * When the menu was opened, or -1 while it is closed.
     */
    private static long openedAt = -1;

    /**
     * Time spent in the menu before its current opening, in ms.
     */
    private static long closedTotal = 0;

    private MenuTime() {
    }

    /**
     * Follows the opening and closing of the menu.
     */
    static void setOpen(final boolean open) {
        synchronized (LOCK) {
            final long now = clock.getAsLong();
            if (open && openedAt < 0) {
                openedAt = now;
            } else if (!open && openedAt >= 0) {
                closedTotal += now - openedAt;
                openedAt = -1;
            }
        }
    }

    /**
     * @return the time spent in the menu since the start of the JVM, the current opening included, in ms
     */
    public static long total() {
        synchronized (LOCK) {
            return closedTotal + (openedAt >= 0 ? clock.getAsLong() - openedAt : 0);
        }
    }

    /**
     * @return a time in ms that does not move while the menu is open: the difference of two of its values is the time
     *   spent playing between them
     */
    public static long gameClock() {
        synchronized (LOCK) {
            return clock.getAsLong() - total();
        }
    }

    /**
     * Replaces the clock and forgets the time spent in the menu (tests only).
     */
    static void resetForTests(final LongSupplier testClock) {
        synchronized (LOCK) {
            clock = testClock;
            openedAt = -1;
            closedTotal = 0;
        }
    }
}
