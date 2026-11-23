package net.gazeplay.commons.utils.stats;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The delays before a selection written in replayData.json (23/11/2026, tester's feedback, point 22).
 */
class SelectionDelaysTest {

    private final AtomicLong now = new AtomicLong(1000);

    @AfterEach
    void reset() {
        SelectionDelays.resetForTests(System::currentTimeMillis);
    }

    @Test
    void shouldGiveTheDelaysOfARoundCutAtItsStartAndItsStop() {
        SelectionDelays.resetForTests(now::get);
        // Before the round
        SelectionDelays.begin();
        now.set(1500);
        SelectionDelays.end();
        // Across its start
        now.set(1800);
        SelectionDelays.begin();
        now.set(2200);
        SelectionDelays.end();
        // Started again while it runs: one period
        now.set(3000);
        SelectionDelays.begin();
        now.set(3400);
        SelectionDelays.begin();
        now.set(4400);
        SelectionDelays.end();
        // Still running at the stop
        now.set(6000);
        SelectionDelays.begin();
        now.set(6300);

        final List<long[]> delays = SelectionDelays.between(2000, 6500);
        assertEquals(3, delays.size());
        assertArrayEquals(new long[]{2000, 2200}, delays.get(0));
        assertArrayEquals(new long[]{3000, 4400}, delays.get(1));
        assertArrayEquals(new long[]{6000, 6500}, delays.get(2));
    }

    @Test
    void shouldGiveNoDelayWithoutOne() {
        SelectionDelays.resetForTests(now::get);
        SelectionDelays.end();
        assertEquals(List.of(), SelectionDelays.between(0, 10_000));
    }
}
