package net.gazeplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    /**
     * The games saved before they became variants of another game (30/10/2026) are replayed with today's game.
     */
    @Test
    void shouldReplayTheFormerGamesWithTheirNewGame() {
        assertArrayEquals(new String[]{"Memory",
                "GroupedGameVariant:MemoryContentLetters:MemoryVisible:2x3:-"},
            ReplayingGameFromJson.formerGame("OpenMemoryLetters", "DimensionDifficultyGameVariant:2:3:Normal"));
        assertArrayEquals(new String[]{"Memory", "GroupedGameVariant:MemoryContentImages:MemoryHidden:Dynamic:-"},
            ReplayingGameFromJson.formerGame("Memory", "DimensionDifficultyGameVariant:2:2:Dynamic"));
        assertEquals("Horses", ReplayingGameFromJson.formerGame("Horses Simplified", "IntGameVariant:3")[0]);
        // A game of Memory saved today, and the other games: unchanged
        assertNull(ReplayingGameFromJson.formerGame("Memory", "GroupedGameVariant:MemoryContentImages:MemoryHidden:2x2:-"));
        assertNull(ReplayingGameFromJson.formerGame("Ninja", "DimensionDifficultyGameVariant:2:3:Normal"));
    }
}
