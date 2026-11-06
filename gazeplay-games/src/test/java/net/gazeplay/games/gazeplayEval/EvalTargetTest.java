package net.gazeplay.games.gazeplayEval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvalTargetTest {

    /**
     * The start button is 160 pixels high on a full HD screen: its ring was 5 pixels thick, next to a border of 3 pixels
     * of the same color.
     */
    @Test
    void shouldDrawAThickRingOnTheEdgeOfASmallTarget() {
        assertEquals(14, EvalTarget.ringWidth(420, 160, false));
    }

    @Test
    void shouldKeepTheRingProportionalOnALargeTarget() {
        assertEquals(30, EvalTarget.ringWidth(1920, 1000, false));
    }

    /**
     * The circle around the fixation cross does not change.
     */
    @Test
    void shouldKeepTheCircleOfTheFixationCross() {
        assertEquals(5, EvalTarget.ringWidth(100, 100, true));
    }
}
