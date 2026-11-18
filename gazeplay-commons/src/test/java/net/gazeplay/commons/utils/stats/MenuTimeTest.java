package net.gazeplay.commons.utils.stats;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Format 6 (17/11/2026): the time spent with the menu of the game screen open is not counted in the durations.
 */
class MenuTimeTest {

    private final AtomicLong now = new AtomicLong(10_000);

    @AfterEach
    void tearDown() {
        MenuTime.resetForTests(System::currentTimeMillis);
    }

    @Test
    void shouldStopTheGameClockWhileTheMenuIsOpen() {
        MenuTime.resetForTests(now::get);
        final long start = MenuTime.gameClock();

        now.addAndGet(1000);
        MenuTime.setOpen(true);
        now.addAndGet(5000);
        assertEquals(5000, MenuTime.total());
        assertEquals(1000, MenuTime.gameClock() - start);

        // Opening it again while it is open changes nothing
        MenuTime.setOpen(true);
        now.addAndGet(500);
        MenuTime.setOpen(false);
        now.addAndGet(2000);

        assertEquals(5500, MenuTime.total());
        assertEquals(3000, MenuTime.gameClock() - start);
    }

    /**
     * The duration of a game read from a file: without its menu time from version 6, whole before (no "menuDuration").
     */
    @Test
    void shouldTakeTheMenuTimeOutOfTheDurationOfAGameReadFromAFile() {
        final LifeCycle version6 = new Gson().fromJson("{\"startTime\":1000,\"stopTime\":9000,\"menuDuration\":2500}",
            LifeCycle.class);
        assertEquals(5500, version6.computeTotalElapsedDuration());

        final LifeCycle version5 = new Gson().fromJson("{\"startTime\":1000,\"stopTime\":9000}", LifeCycle.class);
        assertNull(version5.getMenuDuration());
        assertEquals(8000, version5.computeTotalElapsedDuration());
    }
}
