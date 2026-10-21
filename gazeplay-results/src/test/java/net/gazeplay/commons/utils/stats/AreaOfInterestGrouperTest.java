package net.gazeplay.commons.utils.stats;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaOfInterestGrouperTest {

    private final List<CoordinatesTracker> history = new ArrayList<>();
    private final AreaOfInterestGrouper grouper = new AreaOfInterestGrouper();

    private Optional<AreaOfInterestGrouper.Area> sample(int x, int y, long interval) {
        history.add(new CoordinatesTracker(x, y, interval, 0));
        return history.size() > 1 ? grouper.add(history, history.size() - 1) : Optional.empty();
    }

    @Test
    void shouldCloseAGroupOfMoreThanTwoSamplesOnAJump() {
        sample(100, 100, 20);
        sample(110, 100, 20);
        sample(120, 90, 20);
        AreaOfInterestGrouper.Area area = sample(400, 100, 20).orElseThrow();

        assertEquals(3, area.samples().size());
        assertEquals(0, area.start());
        assertEquals(3, area.end());
        assertArrayEquals(new Double[]{85.0, 115.0, 135.0, 115.0, 135.0, 75.0, 85.0, 75.0}, area.rectangle());
    }

    @Test
    void shouldNotGroupSamplesTooCloseInTime() {
        sample(100, 100, 20);
        sample(110, 100, 5);
        sample(120, 100, 5);

        assertTrue(sample(400, 100, 20).isEmpty());
    }

    @Test
    void shouldMakeASquareAroundTheFirstSampleOfASmallGroupOnALongJump() {
        sample(100, 100, 20);
        sample(900, 100, 20);
        sample(910, 100, 20);
        AreaOfInterestGrouper.Area area = sample(100, 900, 20).orElseThrow();

        assertEquals(2, area.samples().size());
        assertArrayEquals(new Double[]{880.0, 130.0, 940.0, 130.0, 940.0, 70.0, 880.0, 70.0}, area.rectangle());
    }

    /**
     * Without any group (first sample, then a long jump), the square surrounds the sample of the jump: the former code
     * took the first sample of the group, which only holds that sample.
     */
    @Test
    void shouldMakeASquareAroundTheSampleOfTheJumpWithoutGroup() {
        sample(100, 100, 20);
        AreaOfInterestGrouper.Area area = sample(1000, 100, 20).orElseThrow();

        assertEquals(1, area.samples().size());
        assertArrayEquals(new Double[]{970.0, 130.0, 1030.0, 130.0, 1030.0, 70.0, 970.0, 70.0}, area.rectangle());
    }

    @Test
    void shouldIgnoreAShortJumpFromASmallGroup() {
        sample(100, 100, 20);
        sample(110, 100, 20);

        assertTrue(sample(500, 100, 20).isEmpty());
    }
}
