package net.gazeplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplayingGameFromJsonTest {

    /**
     * Each sample is played at its recorded time since the start of the round, whatever its source: the waits of the
     * gaze and of the mouse no longer add up.
     */
    @Test
    void shouldWaitUntilTheRecordedTimeOfTheSample() {
        assertEquals(300, ReplayingGameFromJson.delayBeforeSample(1000, 700));
        assertEquals(0, ReplayingGameFromJson.delayBeforeSample(1000, 1000));
        // a sample slightly earlier than the previous one of the other source (format version 3): no wait
        assertEquals(0, ReplayingGameFromJson.delayBeforeSample(990, 1000));
    }

}
